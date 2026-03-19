package com.quran.tathbeet.sync

data class CloudBackend(
    val authClient: EmailLinkAuthClient,
    val cloudSyncStore: CloudSyncStore,
)
