package com.quran.tathbeet.ui.features.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.BuildConfig
import com.quran.tathbeet.app.LocalReminderScheduler
import com.quran.tathbeet.domain.model.AppSettings
import com.quran.tathbeet.domain.model.AppThemeMode
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.domain.repository.SettingsRepository
import com.quran.tathbeet.sync.AuthSessionRepository
import com.quran.tathbeet.sync.AuthSessionState
import com.quran.tathbeet.sync.EmailLinkRequestResult
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val localReminderScheduler: LocalReminderScheduler,
    private val authSessionRepository: AuthSessionRepository,
) : ViewModel() {
    private val tag = "SettingsViewModel"

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        launchUndispatched { observeUiState() }
    }

    fun toggleGlobalNotifications() {
        val enabled = !_uiState.value.globalNotificationsEnabled
        updateSettingsAndReminders {
            settingsRepository.setGlobalNotificationsEnabled(enabled)
        }
    }

    fun toggleMotivationalMessages() {
        val enabled = !_uiState.value.motivationalMessagesEnabled
        updateSettingsAndReminders {
            settingsRepository.setMotivationalMessagesEnabled(enabled)
        }
    }

    fun selectReminderTime(
        hour: Int,
        minute: Int,
    ) {
        val current = _uiState.value
        if (current.reminderHour == hour && current.reminderMinute == minute) {
            return
        }
        updateSettingsAndReminders {
            settingsRepository.setReminderTime(hour, minute)
        }
    }

    fun selectThemeMode(themeMode: AppThemeMode) {
        launchUndispatched {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun toggleProfileNotifications(profileId: String) {
        val profile = _uiState.value.profiles.firstOrNull { item -> item.id == profileId } ?: return
        updateSettingsAndReminders {
            profileRepository.updateNotificationsEnabled(profileId, !profile.notificationsEnabled)
        }
    }

    fun requestEmailLink(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            Log.w(tag, "requestEmailLink ignored because the email field was blank.")
            return
        }
        launchUndispatched {
            when (val result = authSessionRepository.requestEmailLink(trimmedEmail)) {
                EmailLinkRequestResult.Success -> Unit
                is EmailLinkRequestResult.Error -> logEmailLinkFailure(trimmedEmail, result)
                is EmailLinkRequestResult.ManualSetupRequired -> logEmailLinkFailure(trimmedEmail, result)
            }
        }
    }

    fun signOut() {
        launchUndispatched {
            authSessionRepository.signOut()
        }
    }

    private suspend fun observeUiState() {
        combine(
            settingsRepository.observeSettings(),
            profileRepository.observeAccounts(),
            profileRepository.observeActiveAccount(),
            authSessionRepository.observeSession(),
        ) { settings, accounts, activeAccount, authSession ->
            SettingsInputs(
                settings = settings,
                accounts = accounts,
                activeAccountId = activeAccount?.id,
                authSession = authSession,
            )
        }.collectLatest { inputs ->
            observeSchedules(inputs.accounts).collect { schedules ->
                _uiState.value = inputs.toUiState(schedules)
            }
        }
    }

    private fun observeSchedules(accounts: List<LearnerAccount>) =
        if (accounts.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(accounts.map { account -> scheduleRepository.observeActiveSchedule(account.id) }) { schedules ->
                schedules.toList()
            }
        }

    private fun updateSettingsAndReminders(update: suspend () -> Unit) {
        launchUndispatched {
            update()
            localReminderScheduler.syncSchedules()
        }
    }

    private fun launchUndispatched(block: suspend () -> Unit) {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            block()
        }
    }

    private fun logEmailLinkFailure(
        email: String,
        result: EmailLinkRequestResult,
    ) {
        when (result) {
            is EmailLinkRequestResult.Error ->
                Log.e(
                    tag,
                    "requestEmailLink finished with Error for email=$email message=${result.message}",
                )

            is EmailLinkRequestResult.ManualSetupRequired ->
                Log.e(
                    tag,
                    "requestEmailLink finished with ManualSetupRequired for email=$email reason=${result.reason}",
                )

            EmailLinkRequestResult.Success -> Unit
        }
    }
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val localReminderScheduler: LocalReminderScheduler,
    private val authSessionRepository: AuthSessionRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                settingsRepository = settingsRepository,
                profileRepository = profileRepository,
                scheduleRepository = scheduleRepository,
                localReminderScheduler = localReminderScheduler,
                authSessionRepository = authSessionRepository,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

private data class SettingsInputs(
    val settings: AppSettings,
    val accounts: List<LearnerAccount>,
    val activeAccountId: String?,
    val authSession: AuthSessionState,
)

private fun SettingsInputs.toUiState(
    schedules: List<RevisionSchedule?>,
): SettingsUiState = SettingsUiState(
    isLoading = false,
    themeMode = settings.themeMode,
    globalNotificationsEnabled = settings.globalNotificationsEnabled,
    motivationalMessagesEnabled = settings.motivationalMessagesEnabled,
    reminderHour = settings.reminderHour,
    reminderMinute = settings.reminderMinute,
    profiles = accounts.toProfileUiState(activeAccountId, schedules),
    account = toAccountUiState(),
)

private fun List<LearnerAccount>.toProfileUiState(
    activeAccountId: String?,
    schedules: List<RevisionSchedule?>,
): List<SettingsProfileUiState> = mapIndexed { index, account ->
    SettingsProfileUiState(
        id = account.id,
        name = account.name,
        isActive = account.id == activeAccountId,
        isSelfProfile = account.isSelfProfile,
        notificationsEnabled = account.notificationsEnabled,
        hasSchedule = schedules[index] != null,
    )
}.sortedWith(
    compareByDescending<SettingsProfileUiState> { it.isActive }
        .thenByDescending { it.isSelfProfile }
        .thenByDescending { it.notificationsEnabled }
        .thenBy { it.name },
)

private fun SettingsInputs.toAccountUiState(): SettingsAccountUiState = SettingsAccountUiState(
    isRuntimeConfigured = authSession.isRuntimeConfigured,
    status = authSession.status,
    email = authSession.email,
    pendingEmail = authSession.pendingEmail,
    debugSyncedProfileId = if (BuildConfig.DEBUG) {
        accounts.firstOrNull { it.isSelfProfile }?.cloudProfileId
            ?: authSession.userId?.let { "personal-$it" }
    } else {
        null
    },
)
