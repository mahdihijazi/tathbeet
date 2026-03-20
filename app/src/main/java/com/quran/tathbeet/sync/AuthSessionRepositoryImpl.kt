package com.quran.tathbeet.sync

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class AuthSessionRepositoryImpl(
    private val authClient: EmailLinkAuthClient,
    private val pendingEmailStore: PendingEmailStore,
) : AuthSessionRepository {

    private val tag = "AuthSessionRepository"

    override fun observeSession(): Flow<AuthSessionState> =
        combine(
            authClient.observeCurrentUser(),
            pendingEmailStore.observePendingEmail(),
        ) { currentUser, pendingEmail ->
            when {
                currentUser != null -> AuthSessionState(
                    isRuntimeConfigured = authClient.isConfigured,
                    status = AuthSessionStatus.SignedIn,
                    email = currentUser.email,
                    userId = currentUser.uid,
                    pendingEmail = null,
                )

                pendingEmail != null -> AuthSessionState(
                    isRuntimeConfigured = authClient.isConfigured,
                    status = AuthSessionStatus.LinkSent,
                    email = null,
                    userId = null,
                    pendingEmail = pendingEmail,
                )

                else -> AuthSessionState(
                    isRuntimeConfigured = authClient.isConfigured,
                    status = AuthSessionStatus.SignedOut,
                    email = null,
                    userId = null,
                    pendingEmail = null,
                )
            }
        }
            .distinctUntilChanged()

    override suspend fun requestEmailLink(email: String): EmailLinkRequestResult {
        if (!authClient.isConfigured) {
            Log.e(
                tag,
                "requestEmailLink blocked because auth provider is not configured.",
            )
            return EmailLinkRequestResult.ManualSetupRequired(
                reason = "Cloud auth provider is not configured.",
            )
        }

        val trimmedEmail = email.trim()
        val normalizedEmail = trimmedEmail.normalizeEmail()
        if (normalizedEmail.isBlank()) {
            Log.e(tag, "requestEmailLink rejected because email was blank.")
            return EmailLinkRequestResult.Error(message = "Email is required.")
        }

        return runCatching {
            authClient.sendSignInLink(normalizedEmail)
            pendingEmailStore.setPendingEmail(trimmedEmail)
            EmailLinkRequestResult.Success
        }.getOrElse { throwable ->
            Log.e(
                tag,
                "requestEmailLink failed for email=$normalizedEmail message=${throwable.message}",
                throwable,
            )
            EmailLinkRequestResult.Error(
                message = throwable.message ?: "Failed to send sign-in link.",
            )
        }
    }

    override suspend fun completeEmailLinkSignIn(link: String): EmailLinkCompletionResult {
        if (!authClient.isConfigured) {
            Log.e(
                tag,
                "completeEmailLinkSignIn blocked because auth provider is not configured.",
            )
            return EmailLinkCompletionResult.ManualSetupRequired(
                reason = "Cloud auth provider is not configured.",
            )
        }

        val pendingEmail = pendingEmailStore.getPendingEmail()?.normalizeEmail()
        if (pendingEmail.isNullOrBlank()) {
            Log.e(tag, "completeEmailLinkSignIn failed because no pending email was stored.")
            return EmailLinkCompletionResult.Error(message = "No pending sign-in email found.")
        }
        if (!authClient.isSignInLink(link)) {
            Log.e(tag, "completeEmailLinkSignIn rejected because link was not recognized by the auth provider.")
            return EmailLinkCompletionResult.Error(message = "The link is not a valid sign-in link.")
        }

        return runCatching {
            authClient.signInWithEmailLink(
                email = pendingEmail,
                link = link,
            )
            pendingEmailStore.setPendingEmail(null)
            EmailLinkCompletionResult.Success
        }.getOrElse { throwable ->
            Log.e(
                tag,
                "completeEmailLinkSignIn failed for email=$pendingEmail message=${throwable.message}",
                throwable,
            )
            EmailLinkCompletionResult.Error(
                message = throwable.message ?: "Failed to complete sign-in.",
            )
        }
    }

    override suspend fun signOut() {
        authClient.signOut()
        pendingEmailStore.setPendingEmail(null)
    }
}
