package com.quran.tathbeet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quran.tathbeet.domain.model.ProfileSyncMode

@Entity(tableName = "learner_account")
data class LearnerAccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "is_self_profile")
    val isSelfProfile: Boolean,
    @ColumnInfo(name = "is_shared")
    val isShared: Boolean,
    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    @ColumnInfo(name = "sync_mode")
    val syncMode: String = ProfileSyncMode.LocalOnly.name,
    @ColumnInfo(name = "cloud_profile_id")
    val cloudProfileId: String? = null,
)
