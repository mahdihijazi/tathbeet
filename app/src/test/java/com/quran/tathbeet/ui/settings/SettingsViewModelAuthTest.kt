package com.quran.tathbeet.ui.settings

import com.quran.tathbeet.app.LocalReminderScheduler
import com.quran.tathbeet.domain.model.AppSettings
import com.quran.tathbeet.domain.model.AppThemeMode
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.domain.repository.SettingsRepository
import com.quran.tathbeet.sync.AuthSessionRepository
import com.quran.tathbeet.sync.AuthSessionState
import com.quran.tathbeet.sync.AuthSessionStatus
import com.quran.tathbeet.sync.EmailLinkCompletionResult
import com.quran.tathbeet.sync.EmailLinkRequestResult
import com.quran.tathbeet.testutil.MainDispatcherRule
import com.quran.tathbeet.ui.features.settings.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelAuthTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun requesting_email_link_updates_account_state_to_link_sent() = runTest {
        val authRepository = FakeAuthSessionRepository()
        val viewModel = buildViewModel(authRepository)

        viewModel.requestEmailLink("owner@example.com")
        advanceUntilIdle()

        assertEquals(AuthSessionStatus.LinkSent, viewModel.uiState.value.account.status)
        assertEquals("owner@example.com", viewModel.uiState.value.account.pendingEmail)
    }

    @Test
    fun signed_in_session_is_reflected_in_account_ui_state() = runTest {
        val authRepository = FakeAuthSessionRepository().apply {
            session.value = AuthSessionState(
                isRuntimeConfigured = true,
                status = AuthSessionStatus.SignedIn,
                email = "owner@example.com",
                userId = "uid-owner",
                pendingEmail = null,
            )
        }

        val viewModel = buildViewModel(authRepository)
        advanceUntilIdle()

        assertEquals(AuthSessionStatus.SignedIn, viewModel.uiState.value.account.status)
        assertEquals("owner@example.com", viewModel.uiState.value.account.email)
    }

    @Test
    fun signing_out_clears_the_account_ui_state() = runTest {
        val authRepository = FakeAuthSessionRepository().apply {
            session.value = AuthSessionState(
                isRuntimeConfigured = true,
                status = AuthSessionStatus.SignedIn,
                email = "owner@example.com",
                userId = "uid-owner",
                pendingEmail = null,
            )
        }
        val viewModel = buildViewModel(authRepository)

        advanceUntilIdle()
        viewModel.signOut()
        advanceUntilIdle()

        assertEquals(AuthSessionStatus.SignedOut, viewModel.uiState.value.account.status)
        assertEquals(null, viewModel.uiState.value.account.email)
    }

    private fun buildViewModel(authRepository: FakeAuthSessionRepository): SettingsViewModel =
        SettingsViewModel(
            settingsRepository = FakeSettingsRepository(),
            profileRepository = FakeProfileRepository(),
            scheduleRepository = FakeScheduleRepository(),
            localReminderScheduler = NoOpLocalReminderScheduler,
            authSessionRepository = authRepository,
        )
}

private class FakeAuthSessionRepository : AuthSessionRepository {
    val session = MutableStateFlow(
        AuthSessionState(
            isRuntimeConfigured = true,
            status = AuthSessionStatus.SignedOut,
            email = null,
            userId = null,
            pendingEmail = null,
        ),
    )

    override fun observeSession(): Flow<AuthSessionState> = session

    override suspend fun requestEmailLink(email: String): EmailLinkRequestResult {
        session.value = session.value.copy(
            status = AuthSessionStatus.LinkSent,
            pendingEmail = email,
        )
        return EmailLinkRequestResult.Success
    }

    override suspend fun completeEmailLinkSignIn(link: String): EmailLinkCompletionResult =
        EmailLinkCompletionResult.Success

    override suspend fun signOut() {
        session.value = session.value.copy(
            status = AuthSessionStatus.SignedOut,
            email = null,
            userId = null,
            pendingEmail = null,
        )
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private val settings = MutableStateFlow(AppSettings())

    override fun observeSettings(): Flow<AppSettings> = settings

    override suspend fun markScheduleIntroSeen() = Unit

    override suspend fun setGlobalNotificationsEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(globalNotificationsEnabled = enabled)
    }

    override suspend fun setMotivationalMessagesEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(motivationalMessagesEnabled = enabled)
    }

    override suspend fun setReminderTime(hour: Int, minute: Int) {
        settings.value = settings.value.copy(
            reminderHour = hour,
            reminderMinute = minute,
        )
    }

    override suspend fun setThemeMode(themeMode: AppThemeMode) = Unit
}

private class FakeProfileRepository : ProfileRepository {
    private val accounts = MutableStateFlow(
        listOf(
            LearnerAccount(
                id = "self",
                name = "صاحب الحساب",
                isSelfProfile = true,
                isShared = false,
                notificationsEnabled = true,
            ),
        ),
    )

    override fun observeAccounts(): Flow<List<LearnerAccount>> = accounts

    override fun observeActiveAccount(): Flow<LearnerAccount?> = flowOf(accounts.value.first())

    override suspend fun ensureDefaultAccount(name: String) = Unit

    override suspend fun createProfile(name: String): LearnerAccount = accounts.value.first()

    override suspend fun updateAccountName(accountId: String, name: String) = Unit

    override suspend fun updateNotificationsEnabled(accountId: String, enabled: Boolean) = Unit

    override suspend fun deleteProfile(accountId: String) = Unit

    override suspend fun setActiveAccount(accountId: String) = Unit
}

private class FakeScheduleRepository : ScheduleRepository {
    override fun observeActiveSchedule(learnerId: String) = flowOf(null)

    override suspend fun saveSchedule(schedule: com.quran.tathbeet.domain.model.RevisionSchedule) = Unit
}

private object NoOpLocalReminderScheduler : LocalReminderScheduler {
    override suspend fun syncSchedules() = Unit

    override suspend fun cancelProfile(profileId: String) = Unit

    override suspend fun handleReminder(profileId: String) = Unit
}
