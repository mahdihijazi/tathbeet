package com.quran.tathbeet.sync

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

object DefaultCloudBackendFactory : CloudBackendFactory {
    override fun create(context: Context): CloudBackend {
        val appContext = context.applicationContext
        val runtimeConfig = FirebaseRuntimeConfigProvider.fromContext(appContext)
        val authClient = FirebaseEmailLinkAuthClient(
            appContext = appContext,
            runtimeConfig = runtimeConfig,
        )
        val cloudSyncStore = if (FirebaseBootstrapper.ensureInitialized(appContext, runtimeConfig)) {
            FirestoreCloudSyncStore(FirebaseFirestore.getInstance())
        } else {
            DisabledCloudSyncStore
        }
        return CloudBackend(
            authClient = authClient,
            cloudSyncStore = cloudSyncStore,
        )
    }
}
