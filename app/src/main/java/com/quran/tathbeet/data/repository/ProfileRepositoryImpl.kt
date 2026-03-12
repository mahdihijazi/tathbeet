package com.quran.tathbeet.data.repository

import androidx.room.withTransaction
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.data.local.entity.LearnerAccountEntity
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.repository.ProfileRepository
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

    override suspend fun updateAccountName(
        accountId: String,
        name: String,
    ) {
        database.learnerAccountDao().updateName(
            accountId = accountId,
            name = name,
        )
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
