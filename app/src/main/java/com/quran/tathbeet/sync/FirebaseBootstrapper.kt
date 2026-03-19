package com.quran.tathbeet.sync

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseBootstrapper {
    private const val TAG = "FirebaseBootstrapper"

    fun ensureInitialized(
        context: Context,
        config: FirebaseRuntimeConfig,
    ): Boolean {
        if (!config.isConfigured) {
            Log.w(TAG, "Firebase bootstrap skipped because runtime config is not fully configured.")
            return false
        }
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            Log.d(TAG, "Firebase already initialized.")
            return true
        }

        return runCatching {
            val options = FirebaseOptions.Builder()
                .setApiKey(config.apiKey)
                .setApplicationId(config.applicationId)
                .setProjectId(config.projectId)
                .setStorageBucket(config.storageBucket)
                .build()

            FirebaseApp.initializeApp(context, options)
            Log.i(
                TAG,
                "Firebase initialized for projectId=${config.projectId} applicationId=${config.applicationId}",
            )
            true
        }.getOrElse { throwable ->
            Log.e(
                TAG,
                "Firebase initialization failed for projectId=${config.projectId} message=${throwable.message}",
                throwable,
            )
            false
        }
    }
}
