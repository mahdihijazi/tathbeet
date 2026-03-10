package com.quran.tathbeet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
)
