package com.quran.tathbeet.domain.model

data class LearnerAccount(
    val id: String,
    val name: String,
    val isSelfProfile: Boolean,
    val isShared: Boolean,
    val notificationsEnabled: Boolean,
)
