package com.quran.tathbeet.sync

import android.content.Context
import android.util.Log
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirebaseEmailLinkAuthClient(
    private val appContext: Context,
    private val runtimeConfig: FirebaseRuntimeConfig,
) : EmailLinkAuthClient {

    private val tag = "FirebaseEmailLinkAuth"

    override val isConfigured: Boolean
        get() = runtimeConfig.isConfigured

    override fun observeCurrentUser(): Flow<AuthUser?> {
        if (!FirebaseBootstrapper.ensureInitialized(appContext, runtimeConfig)) {
            Log.w(tag, "observeCurrentUser returning null flow because Firebase bootstrap failed.")
            return flowOf(null)
        }
        val auth = FirebaseAuth.getInstance()
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                trySend(
                    user?.let { resolvedUser ->
                        AuthUser(
                            uid = resolvedUser.uid,
                            email = resolvedUser.email,
                        )
                    },
                )
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }
    }

    override suspend fun sendSignInLink(email: String) {
        val auth = requireAuth()
        runCatching {
            auth.sendSignInLinkToEmail(
                email,
                ActionCodeSettings.newBuilder()
                    .setUrl(runtimeConfig.emailLinkUrl)
                    .setHandleCodeInApp(true)
                    .setAndroidPackageName(
                        runtimeConfig.androidPackageName,
                        true,
                        null,
                    )
                    .build(),
            ).await()
        }.getOrElse { throwable ->
            Log.e(
                tag,
                "sendSignInLink Firebase call failed email=$email code=${firebaseErrorCode(throwable)} message=${throwable.message}",
                throwable,
            )
            throw throwable
        }
    }

    override fun isSignInLink(link: String): Boolean {
        val isBootstrapReady = FirebaseBootstrapper.ensureInitialized(appContext, runtimeConfig)
        return isBootstrapReady && FirebaseAuth.getInstance().isSignInWithEmailLink(link)
    }

    override suspend fun signInWithEmailLink(
        email: String,
        link: String,
    ): AuthUser {
        val authResult = runCatching {
            requireAuth().signInWithEmailLink(email, link).await()
        }.getOrElse { throwable ->
            Log.e(
                tag,
                "signInWithEmailLink Firebase call failed email=$email code=${firebaseErrorCode(throwable)} message=${throwable.message}",
                throwable,
            )
            throw throwable
        }
        val user = authResult.user ?: error("Firebase sign-in completed without a user.")
        return AuthUser(
            uid = user.uid,
            email = user.email,
        )
    }

    override suspend fun signOut() {
        if (!FirebaseBootstrapper.ensureInitialized(appContext, runtimeConfig)) {
            Log.w(tag, "signOut skipped because Firebase bootstrap failed.")
            return
        }
        FirebaseAuth.getInstance().signOut()
    }

    private fun requireAuth(): FirebaseAuth {
        check(FirebaseBootstrapper.ensureInitialized(appContext, runtimeConfig)) {
            "Firebase is not configured. Fill the placeholder values first."
        }
        return FirebaseAuth.getInstance()
    }

    private fun firebaseErrorCode(throwable: Throwable): String? =
        (throwable as? FirebaseAuthException)?.errorCode
}
