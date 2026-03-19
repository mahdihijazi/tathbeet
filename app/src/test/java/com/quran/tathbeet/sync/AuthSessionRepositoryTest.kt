package com.quran.tathbeet.sync

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSessionRepositoryTest {

    @Test
    fun requesting_email_link_persists_pending_email() = runTest {
        val repository = buildRepository()

        val result = repository.requestEmailLink("teacher@example.com")

        assertTrue(result is EmailLinkRequestResult.Success)
        val state = repository.observeSession().first()
        assertEquals(AuthSessionStatus.LinkSent, state.status)
        assertEquals("teacher@example.com", state.pendingEmail)
    }

    @Test
    fun configured_repository_completes_sign_in_and_clears_pending_email() = runTest {
        val repository = buildRepository()
        repository.requestEmailLink("owner@example.com")

        val result = repository.completeEmailLinkSignIn("https://example.com/link")

        assertTrue(result is EmailLinkCompletionResult.Success)
        val state = repository.observeSession().first()
        assertEquals(AuthSessionStatus.SignedIn, state.status)
        assertEquals("owner@example.com", state.email)
        assertEquals(null, state.pendingEmail)
    }

    @Test
    fun requesting_email_link_normalizes_address_for_auth_client_and_keeps_trimmed_pending_email() = runTest {
        val authClient = FakeEmailLinkAuthClient()
        val repository = AuthSessionRepositoryImpl(
            authClient = authClient,
            pendingEmailStore = InMemoryPendingEmailStore(),
        )

        val result = repository.requestEmailLink("  OWNER@Example.com  ")
        val state = repository.observeSession().first()

        assertTrue(result is EmailLinkRequestResult.Success)
        assertEquals(listOf("owner@example.com"), authClient.sentSignInLinks)
        assertEquals("OWNER@Example.com", state.pendingEmail)
    }

    @Test
    fun completing_sign_in_normalizes_pending_email_before_authentication() = runTest {
        val authClient = FakeEmailLinkAuthClient()
        val pendingEmailStore = InMemoryPendingEmailStore()
        pendingEmailStore.setPendingEmail("  OWNER@Example.com  ")
        val repository = AuthSessionRepositoryImpl(
            authClient = authClient,
            pendingEmailStore = pendingEmailStore,
        )

        val result = repository.completeEmailLinkSignIn("https://example.com/link")
        val state = repository.observeSession().first()

        assertTrue(result is EmailLinkCompletionResult.Success)
        assertEquals(listOf("owner@example.com"), authClient.signInEmails)
        assertEquals(AuthSessionStatus.SignedIn, state.status)
        assertEquals("owner@example.com", state.email)
        assertEquals(null, state.pendingEmail)
    }

    @Test
    fun unconfigured_repository_reports_manual_setup_required() = runTest {
        val authClient = FakeEmailLinkAuthClient(isConfigured = false)
        val repository = AuthSessionRepositoryImpl(
            authClient = authClient,
            pendingEmailStore = InMemoryPendingEmailStore(),
        )

        val requestResult = repository.requestEmailLink("owner@example.com")
        val completionResult = repository.completeEmailLinkSignIn("https://example.com/link")
        val state = repository.observeSession().first()

        assertTrue(requestResult is EmailLinkRequestResult.ManualSetupRequired)
        assertTrue(completionResult is EmailLinkCompletionResult.ManualSetupRequired)
        assertEquals(AuthSessionStatus.SignedOut, state.status)
    }

    private fun buildRepository(): AuthSessionRepositoryImpl =
        AuthSessionRepositoryImpl(
            authClient = FakeEmailLinkAuthClient(),
            pendingEmailStore = InMemoryPendingEmailStore(),
        )
}
