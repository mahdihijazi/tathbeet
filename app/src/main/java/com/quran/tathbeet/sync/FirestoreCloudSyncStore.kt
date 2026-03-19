package com.quran.tathbeet.sync

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.ReviewDay
import java.util.Collections
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreCloudSyncStore(
    private val firestore: FirebaseFirestore,
) : CloudSyncStore {
    private val tag = "FirestoreCloudSync"
    private val writeBlockedProfileIds = Collections.synchronizedSet(mutableSetOf<String>())

    override fun observeAccessibleProfiles(userId: String): Flow<List<CloudProfileSummary>> =
        callbackFlow {
            val registration = accessibleProfiles(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            tag,
                            "observeAccessibleProfiles failed for userId=$userId message=${error.message}",
                            error,
                        )
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot.toCloudProfileSummaries())
                }
            awaitClose { registration.remove() }
        }

    override fun observeProfileSnapshot(cloudProfileId: String): Flow<CloudProfileSnapshot?> =
        flow {
            val snapshot = runCatching { fetchProfileSnapshot(cloudProfileId) }
                .onFailure { throwable ->
                    Log.e(
                        tag,
                        "observeProfileSnapshot initial load failed for cloudProfileId=$cloudProfileId message=${throwable.message}",
                        throwable,
                    )
                }
                .getOrNull()
            emit(snapshot)
        }

    override suspend fun claimAccessibleProfiles(user: AuthUser): List<CloudProfileSummary> {
        val email = user.email?.normalizeEmail() ?: return emptyList()
        val memberships = runCatching {
            firestore.collectionGroup("members")
                .whereEqualTo("email", email)
                .get()
                .await()
        }.getOrElse { throwable ->
            if (throwable is FirebaseFirestoreException) {
                Log.w(
                    tag,
                    "claimAccessibleProfiles skipped for email=$email code=${throwable.code} message=${throwable.message}",
                )
                return accessibleProfiles(user.uid).get().await().toCloudProfileSummaries()
            }
            throw throwable
        }

        memberships.documents.forEach { memberDocument ->
            val profileRef = memberDocument.reference.parent.parent ?: return@forEach
            val profileDocument = profileRef.get().await()
            if (!profileDocument.exists()) return@forEach

            val role = memberDocument.getString("role").toMemberRole()
            val summary = CloudProfileSummary(
                cloudProfileId = profileRef.id,
                displayName = profileDocument.getString("displayName").orEmpty(),
                ownerEmail = profileDocument.getString("ownerEmail"),
                syncMode = role.toProfileSyncMode(
                    isShared = profileDocument.getBoolean("isShared") ?: false,
                ),
                memberRole = role,
            )

            accessibleProfiles(user.uid).document(profileRef.id).set(
                mapOf(
                    "displayName" to summary.displayName,
                    "ownerEmail" to summary.ownerEmail,
                    "syncMode" to summary.syncMode.name,
                    "role" to summary.memberRole.name,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()

            memberDocument.reference.update(
                mapOf(
                    "userId" to user.uid,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
        }

        return accessibleProfiles(user.uid).get().await().toCloudProfileSummaries()
    }

    override suspend fun fetchProfileSnapshot(cloudProfileId: String): CloudProfileSnapshot? {
        return runCatching {
            val profileRef = profiles().document(cloudProfileId)
            val profileDocument = profileRef.get().await()
            if (!profileDocument.exists()) {
                return null
            }

            val planDocument = profileRef.collection("plan").document("meta").get().await()
            val reviewDays = profileRef.collection("reviewDays").get().await()
            val tasks = profileRef.collection("tasks").get().await()

            val dayCompletionRates = reviewDays.documents.associate { document ->
                document.id to document.getLong("completionRate")?.toInt().orZero()
            }
            val assignments = tasks.documents.mapNotNull { document ->
                document.toReviewAssignment(profileId = cloudProfileId)
            }
            val reviewTimeline = assignments
                .groupBy { assignment -> assignment.assignedForDate }
                .toSortedMap()
                .map { (assignedForDate, items) ->
                    ReviewDay(
                        learnerId = cloudProfileId,
                        assignedForDate = assignedForDate,
                        completionRate = dayCompletionRates[assignedForDate.toString()]
                            ?: completionRateFor(items),
                        assignments = items.sortedBy { assignment -> assignment.displayOrder },
                    )
                }

            CloudProfileSnapshot(
                cloudProfileId = cloudProfileId,
                localProfileId = cloudProfileId,
                displayName = profileDocument.getString("displayName").orEmpty(),
                syncMode = profileDocument.getString("syncMode")
                    ?.let { value -> enumValueOf<ProfileSyncMode>(value) }
                    ?: ProfileSyncMode.LocalOnly,
                ownerUserId = profileDocument.getString("ownerUserId").orEmpty(),
                ownerEmail = profileDocument.getString("ownerEmail"),
                schedule = planDocument.toRevisionSchedule(profileId = cloudProfileId),
                reviewDays = reviewTimeline,
            )
        }.getOrElse { throwable ->
            if (throwable is FirebaseFirestoreException) {
                Log.w(
                    tag,
                    "fetchProfileSnapshot skipped for cloudProfileId=$cloudProfileId code=${throwable.code} message=${throwable.message}",
                )
                return null
            }
            throw throwable
        }
    }

    override suspend fun upsertOwnedProfile(snapshot: CloudProfileSnapshot) {
        if (snapshot.cloudProfileId in writeBlockedProfileIds) {
            Log.i(
                tag,
                "upsertOwnedProfile skipped for cloudProfileId=${snapshot.cloudProfileId} because writes were already denied in this session",
            )
            return
        }
        runCatching {
            val profileRef = profiles().document(snapshot.cloudProfileId)
            val ownerMemberRef = profileRef.collection("members").document(snapshot.ownerEmail.documentId())
            val ownerAccessibleRef = accessibleProfiles(snapshot.ownerUserId).document(snapshot.cloudProfileId)
            val planRef = profileRef.collection("plan").document("meta")

            val existingReviewDays = runCatching {
                profileRef.collection("reviewDays").get().await()
            }.getOrElse { throwable ->
                if (throwable is FirebaseFirestoreException) {
                    Log.w(
                        tag,
                        "upsertOwnedProfile reviewDays prefetch skipped for cloudProfileId=${snapshot.cloudProfileId} code=${throwable.code} message=${throwable.message}",
                    )
                    null
                } else {
                    throw throwable
                }
            }
            val existingTasks = runCatching {
                profileRef.collection("tasks").get().await()
            }.getOrElse { throwable ->
                if (throwable is FirebaseFirestoreException) {
                    Log.w(
                        tag,
                        "upsertOwnedProfile tasks prefetch skipped for cloudProfileId=${snapshot.cloudProfileId} code=${throwable.code} message=${throwable.message}",
                    )
                    null
                } else {
                    throw throwable
                }
            }
            val desiredReviewDayIds = snapshot.reviewDays.mapTo(linkedSetOf()) { day ->
                day.assignedForDate.toString()
            }
            val desiredTaskIds = snapshot.reviewDays
                .flatMap { day -> day.assignments }
                .mapTo(linkedSetOf()) { assignment -> assignment.id }

            val batch = firestore.batch()
            existingReviewDays?.documents
                ?.filterNot { document -> document.id in desiredReviewDayIds }
                ?.forEach { document -> batch.delete(document.reference) }
            existingTasks?.documents
                ?.filterNot { document -> document.id in desiredTaskIds }
                ?.forEach { document -> batch.delete(document.reference) }

            batch.set(
                profileRef,
                mapOf(
                    "displayName" to snapshot.displayName,
                    "ownerUserId" to snapshot.ownerUserId,
                    "ownerEmail" to snapshot.ownerEmail,
                    "syncMode" to snapshot.syncMode.name,
                    "isShared" to (snapshot.syncMode == ProfileSyncMode.SharedOwner),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            batch.set(
                ownerAccessibleRef,
                mapOf(
                    "displayName" to snapshot.displayName,
                    "ownerEmail" to snapshot.ownerEmail,
                    "syncMode" to snapshot.syncMode.name,
                    "role" to CloudProfileMemberRole.Owner.name,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            batch.set(
                ownerMemberRef,
                mapOf(
                    "email" to snapshot.ownerEmail.normalizeEmail(),
                    "userId" to snapshot.ownerUserId,
                    "role" to CloudProfileMemberRole.Owner.name,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            batch.set(
                planRef,
                snapshot.schedule.toPlanMap(),
            )

            snapshot.reviewDays.forEach { day ->
                batch.set(
                    profileRef.collection("reviewDays").document(day.assignedForDate.toString()),
                    mapOf(
                        "assignedForDate" to day.assignedForDate.toString(),
                        "completionRate" to day.completionRate,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                day.assignments.forEach { assignment ->
                    batch.set(
                        profileRef.collection("tasks").document(assignment.id),
                        assignment.toTaskMap(),
                    )
                }
            }
            batch.commit().await()
        }.getOrElse { throwable ->
            if (throwable is FirebaseFirestoreException) {
                writeBlockedProfileIds += snapshot.cloudProfileId
                Log.w(
                    tag,
                    "upsertOwnedProfile skipped for cloudProfileId=${snapshot.cloudProfileId} code=${throwable.code} message=${throwable.message}",
                )
                return
            }
            throw throwable
        }
    }

    override suspend fun listMembers(cloudProfileId: String): List<CloudProfileMember> =
        profiles().document(cloudProfileId)
            .collection("members")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                val email = document.getString("email") ?: return@mapNotNull null
                CloudProfileMember(
                    email = email,
                    role = document.getString("role").toMemberRole(),
                    userId = document.getString("userId"),
                )
            }
            .sortedBy { member -> member.email }

    override suspend fun inviteEditor(
        cloudProfileId: String,
        ownerUserId: String,
        email: String,
    ) {
        val normalizedEmail = email.normalizeEmail()
        profiles().document(cloudProfileId)
            .collection("members")
            .document(normalizedEmail.documentId())
            .set(
                mapOf(
                    "email" to normalizedEmail,
                    "role" to CloudProfileMemberRole.Editor.name,
                    "invitedBy" to ownerUserId,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            .await()
    }

    override suspend fun removeMember(
        cloudProfileId: String,
        email: String,
    ) {
        val memberRef = profiles().document(cloudProfileId)
            .collection("members")
            .document(email.normalizeEmail().documentId())
        val memberSnapshot = memberRef.get().await()
        memberSnapshot.getString("userId")?.let { userId ->
            accessibleProfiles(userId).document(cloudProfileId).delete().await()
        }
        memberRef.delete().await()
    }

    override suspend fun deleteProfile(cloudProfileId: String) {
        val profileRef = profiles().document(cloudProfileId)
        val members = profileRef.collection("members").get().await()
        val reviewDays = profileRef.collection("reviewDays").get().await()
        val tasks = profileRef.collection("tasks").get().await()
        val batch = firestore.batch()

        members.documents.forEach { document ->
            document.getString("userId")?.let { userId ->
                batch.delete(accessibleProfiles(userId).document(cloudProfileId))
            }
            batch.delete(document.reference)
        }
        reviewDays.documents.forEach { document -> batch.delete(document.reference) }
        tasks.documents.forEach { document -> batch.delete(document.reference) }
        batch.delete(profileRef.collection("plan").document("meta"))
        batch.delete(profileRef)
        batch.commit().await()
    }

    private fun accessibleProfiles(userId: String) =
        firestore.collection("users").document(userId).collection("accessibleProfiles")

    private fun profiles() = firestore.collection("profiles")

}
