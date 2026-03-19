package com.quran.tathbeet.sync

import android.content.Context
import com.quran.tathbeet.BuildConfig

object FirebaseRuntimeConfigProvider {
    fun fromContext(context: Context): FirebaseRuntimeConfig =
        FirebaseRuntimeConfig(
            apiKey = context.findStringResource("google_api_key") ?: BuildConfig.FIREBASE_API_KEY,
            applicationId = context.findStringResource("google_app_id") ?: BuildConfig.FIREBASE_APPLICATION_ID,
            projectId = context.findStringResource("project_id") ?: BuildConfig.FIREBASE_PROJECT_ID,
            storageBucket = context.findStringResource("google_storage_bucket") ?: BuildConfig.FIREBASE_STORAGE_BUCKET,
            authDomain = BuildConfig.FIREBASE_AUTH_DOMAIN,
            authHost = BuildConfig.FIREBASE_AUTH_HOST,
            androidPackageName = BuildConfig.FIREBASE_ANDROID_PACKAGE_NAME,
            emailLinkUrl = BuildConfig.FIREBASE_EMAIL_LINK_URL,
        )

    private fun Context.findStringResource(name: String): String? {
        val resourceId = resources.getIdentifier(name, "string", packageName)
        return resourceId
            .takeIf { it != 0 }
            ?.let(resources::getString)
            ?.takeIf(String::isNotBlank)
    }
}
