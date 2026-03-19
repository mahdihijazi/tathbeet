package com.quran.tathbeet.sync

enum class AuthSessionStatus {
    SignedOut,
    LinkSent,
    SignedIn,
}

data class AuthSessionState(
    val isRuntimeConfigured: Boolean,
    val status: AuthSessionStatus,
    val email: String?,
    val userId: String?,
    val pendingEmail: String?,
)

data class AuthUser(
    val uid: String,
    val email: String?,
)

sealed interface EmailLinkRequestResult {
    data object Success : EmailLinkRequestResult

    data class ManualSetupRequired(
        val reason: String,
    ) : EmailLinkRequestResult

    data class Error(
        val message: String,
    ) : EmailLinkRequestResult
}

sealed interface EmailLinkCompletionResult {
    data object Success : EmailLinkCompletionResult

    data class ManualSetupRequired(
        val reason: String,
    ) : EmailLinkCompletionResult

    data class Error(
        val message: String,
    ) : EmailLinkCompletionResult
}
