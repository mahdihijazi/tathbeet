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

    @Query("SELECT COUNT(*) FROM learner_account")
    suspend fun accountCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LearnerAccountEntity)

    @Query("UPDATE learner_account SET name = :name WHERE id = :accountId")
    suspend fun updateName(accountId: String, name: String)

    @Query("UPDATE learner_account SET is_active = 0")
    suspend fun clearActiveAccount()

    @Query("UPDATE learner_account SET is_active = 1 WHERE id = :accountId")
    suspend fun setActiveAccount(accountId: String)
}
