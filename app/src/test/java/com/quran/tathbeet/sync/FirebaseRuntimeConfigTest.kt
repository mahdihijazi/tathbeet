package com.quran.tathbeet.sync

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseRuntimeConfigTest {

    @Test
    fun placeholder_config_is_not_treated_as_configured() {
        val config = FirebaseRuntimeConfig(
            apiKey = "TODO_FIREBASE_API_KEY",
            applicationId = "TODO_FIREBASE_APPLICATION_ID",
            projectId = "TODO_FIREBASE_PROJECT_ID",
            storageBucket = "TODO_FIREBASE_STORAGE_BUCKET",
            authDomain = "TODO_FIREBASE_AUTH_DOMAIN",
            authHost = "TODO_FIREBASE_AUTH_HOST",
            androidPackageName = "com.quran.tathbeet",
            emailLinkUrl = "TODO_FIREBASE_EMAIL_LINK_URL",
        )

        assertFalse(config.isConfigured)
    }

    @Test
    fun complete_config_is_treated_as_configured() {
        val config = FirebaseRuntimeConfig(
            apiKey = "real-api-key",
            applicationId = "1:123456:android:abcdef",
            projectId = "tathbeet-prod",
            storageBucket = "tathbeet-prod.appspot.com",
            authDomain = "tathbeet-prod.firebaseapp.com",
            authHost = "tathbeet-prod.firebaseapp.com",
            androidPackageName = "com.quran.tathbeet",
            emailLinkUrl = "https://tathbeet-prod.firebaseapp.com/auth",
        )

        assertTrue(config.isConfigured)
    }
}
