package com.quran.tathbeet.sync

data class FirebaseRuntimeConfig(
    val apiKey: String,
    val applicationId: String,
    val projectId: String,
    val storageBucket: String,
    val authDomain: String,
    val authHost: String,
    val androidPackageName: String,
    val emailLinkUrl: String,
) {
    val isConfigured: Boolean
        get() = requiredValues.all { value ->
            value.isNotBlank() && !value.startsWith(PlaceholderPrefix)
        }

    private val requiredValues: List<String>
        get() = listOf(
            apiKey,
            applicationId,
            projectId,
            storageBucket,
            authDomain,
            authHost,
            androidPackageName,
            emailLinkUrl,
        )

    companion object {
        private const val PlaceholderPrefix = "TODO_"

        fun placeholders(): FirebaseRuntimeConfig =
            FirebaseRuntimeConfig(
                apiKey = "TODO_FIREBASE_API_KEY",
                applicationId = "TODO_FIREBASE_APPLICATION_ID",
                projectId = "TODO_FIREBASE_PROJECT_ID",
                storageBucket = "TODO_FIREBASE_STORAGE_BUCKET",
                authDomain = "TODO_FIREBASE_AUTH_DOMAIN",
                authHost = "TODO_FIREBASE_AUTH_HOST",
                androidPackageName = "TODO_FIREBASE_ANDROID_PACKAGE_NAME",
                emailLinkUrl = "TODO_FIREBASE_EMAIL_LINK_URL",
            )
    }
}
