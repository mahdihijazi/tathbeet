package com.quran.tathbeet.data.repository

import androidx.room.withTransaction
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.data.local.entity.LearnerAccountEntity
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.repository.ProfileRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepositoryImpl(
    private val database: TathbeetDatabase,
) : ProfileRepository {

    override fun observeAccounts(): Flow<List<LearnerAccount>> =
        database.learnerAccountDao()
            .observeAccounts()
            .map { entities -> entities.map { entity -> entity.toDomainModel() } }

    override fun observeActiveAccount(): Flow<LearnerAccount?> =
        database.learnerAccountDao()
            .observeActiveAccount()
            .map { entity -> entity?.toDomainModel() }

    override suspend fun ensureDefaultAccount(name: String) {
        if (database.learnerAccountDao().accountCount() > 0) {
            return
        }

        database.learnerAccountDao().upsert(
            LearnerAccountEntity(
                id = DEFAULT_ACCOUNT_ID,
                name = name,
                isSelfProfile = true,
                isShared = false,
                notificationsEnabled = true,
                isActive = true,
            ),
        )
    }

    override suspend fun createProfile(name: String): LearnerAccount {
        val entity = LearnerAccountEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            isSelfProfile = false,
            isShared = false,
            notificationsEnabled = true,
            isActive = true,
        )
        database.withTransaction {
            database.learnerAccountDao().clearActiveAccount()
            database.learnerAccountDao().upsert(entity)
        }
        return entity.toDomainModel()
    }

    override suspend fun updateAccountName(
        accountId: String,
        name: String,
    ) {
        database.learnerAccountDao().updateName(
            accountId = accountId,
            name = name,
        )
    }

    override suspend fun updateNotificationsEnabled(
        accountId: String,
        enabled: Boolean,
    ) {
        database.learnerAccountDao().updateNotificationsEnabled(
            accountId = accountId,
            enabled = enabled,
        )
    }

    override suspend fun deleteProfile(accountId: String) {
        database.withTransaction {
            val account = database.learnerAccountDao().getAccount(accountId) ?: return@withTransaction
            if (account.isSelfProfile) {
                return@withTransaction
            }

            database.reviewAssignmentDao().deleteForLearner(accountId)
            database.reviewDayDao().deleteForLearner(accountId)
            database.scheduleSelectionDao().deleteForSchedule("active-$accountId")
            database.revisionScheduleDao().deleteForLearner(accountId)
            database.learnerAccountDao().deleteAccount(accountId)

            if (account.isActive) {
                database.learnerAccountDao().clearActiveAccount()
                database.learnerAccountDao().getPreferredAccount()?.let { fallback ->
                    database.learnerAccountDao().setActiveAccount(fallback.id)
                }
            }
        }
    }

    override suspend fun setActiveAccount(accountId: String) {
        database.withTransaction {
            database.learnerAccountDao().clearActiveAccount()
            database.learnerAccountDao().setActiveAccount(accountId)
        }
    }

    private fun LearnerAccountEntity.toDomainModel(): LearnerAccount =
        LearnerAccount(
            id = id,
            name = name,
            isSelfProfile = isSelfProfile,
            isShared = isShared,
            notificationsEnabled = notificationsEnabled,
        )

    companion object {
        const val DEFAULT_ACCOUNT_ID = "self"
    }
}
