package com.quran.tathbeet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quran.tathbeet.data.local.entity.LearnerAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearnerAccountDao {
    @Query("SELECT * FROM learner_account ORDER BY name")
    fun observeAccounts(): Flow<List<LearnerAccountEntity>>

    @Query("SELECT * FROM learner_account WHERE is_active = 1 LIMIT 1")
    fun observeActiveAccount(): Flow<LearnerAccountEntity?>

    @Query("SELECT * FROM learner_account WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveAccount(): LearnerAccountEntity?

    @Query("SELECT * FROM learner_account WHERE id = :accountId LIMIT 1")
    suspend fun getAccount(accountId: String): LearnerAccountEntity?

    @Query("SELECT * FROM learner_account WHERE cloud_profile_id = :cloudProfileId LIMIT 1")
    suspend fun getByCloudProfileId(cloudProfileId: String): LearnerAccountEntity?

    @Query("SELECT * FROM learner_account WHERE cloud_profile_id IS NOT NULL AND is_self_profile = 0")
    suspend fun getSyncedSubAccounts(): List<LearnerAccountEntity>

    @Query(
        """
        SELECT * FROM learner_account
        ORDER BY is_self_profile DESC, name
        LIMIT 1
        """,
    )
    suspend fun getPreferredAccount(): LearnerAccountEntity?

    @Query("SELECT COUNT(*) FROM learner_account")
    suspend fun accountCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LearnerAccountEntity)

    @Query("UPDATE learner_account SET name = :name WHERE id = :accountId")
    suspend fun updateName(accountId: String, name: String)

    @Query("UPDATE learner_account SET notifications_enabled = :enabled WHERE id = :accountId")
    suspend fun updateNotificationsEnabled(
        accountId: String,
        enabled: Boolean,
    )

    @Query("UPDATE learner_account SET is_active = 0")
    suspend fun clearActiveAccount()

    @Query("UPDATE learner_account SET is_active = 1 WHERE id = :accountId")
    suspend fun setActiveAccount(accountId: String)

    @Query("DELETE FROM learner_account WHERE id = :accountId")
    suspend fun deleteAccount(accountId: String)
}
