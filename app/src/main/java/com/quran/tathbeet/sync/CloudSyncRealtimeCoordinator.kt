package com.quran.tathbeet.sync

import android.util.Log
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CloudSyncRealtimeCoordinator(
    private val authSessionRepository: AuthSessionRepository,
    private val profileRepository: ProfileRepository,
    private val cloudSyncStore: CloudSyncStore,
    private val cloudSyncManager: CloudSyncManager,
) {
    private val tag = "CloudSyncRealtime"

    fun bind(scope: CoroutineScope) {
        scope.launch {
            authSessionRepository.observeSession()
                .map { session ->
                    if (session.status != AuthSessionStatus.SignedIn || session.userId.isNullOrBlank()) {
                        null
                    } else {
                        AuthUser(
                            uid = session.userId,
                            email = session.email,
                        )
                    }
                }
                .distinctUntilChangedBy { user -> user?.uid }
                .collectLatest { user ->
                    if (user != null) {
                        bindForSignedInUser(user)
                    }
                }
        }
    }

    private suspend fun bindForSignedInUser(user: AuthUser) = coroutineScope {
        launch { bindAccessibleProfiles(user) }
        launch { bindActiveProfileSnapshot() }
    }

    private suspend fun bindAccessibleProfiles(user: AuthUser) {
        cloudSyncStore.observeAccessibleProfiles(user.uid)
            .catch { throwable ->
                Log.e(
                    tag,
                    "observeAccessibleProfiles failed for userId=${user.uid} message=${throwable.message}",
                    throwable,
                )
            }
            .collectLatest {
                cloudSyncManager.importAccessibleSharedProfiles(user)
            }
    }

    private suspend fun bindActiveProfileSnapshot() {
        profileRepository.observeActiveAccount()
            .map { account -> account.toActiveCloudProfileTarget() }
            .distinctUntilChanged()
            .filterNotNull()
            .collectLatest { target ->
                cloudSyncStore.observeProfileSnapshot(target.cloudProfileId)
                    .catch { throwable ->
                        Log.e(
                            tag,
                            "observeProfileSnapshot failed for cloudProfileId=${target.cloudProfileId} message=${throwable.message}",
                            throwable,
                        )
                    }
                    .collectLatest { snapshot ->
                        if (snapshot != null) {
                            cloudSyncManager.importRemoteSnapshotForActiveProfile(target.accountId)
                        }
                    }
            }
    }
}

private data class ActiveCloudProfileTarget(
    val accountId: String,
    val cloudProfileId: String,
)

private fun com.quran.tathbeet.domain.model.LearnerAccount?.toActiveCloudProfileTarget(): ActiveCloudProfileTarget? =
    this
        ?.takeIf { candidate ->
            candidate.cloudProfileId != null &&
                candidate.syncMode != ProfileSyncMode.LocalOnly
        }
        ?.let { account ->
            ActiveCloudProfileTarget(
                accountId = account.id,
                cloudProfileId = account.cloudProfileId!!,
            )
        }
