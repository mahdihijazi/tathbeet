package com.quran.tathbeet.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.app.LocalReminderScheduler
import com.quran.tathbeet.domain.model.AppThemeMode
import com.quran.tathbeet.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val localReminderScheduler: LocalReminderScheduler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        launchUndispatched {
            settingsRepository.observeSettings().collect { settings ->
                _uiState.value = settings.toUiState()
            }
        }
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
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val localReminderScheduler: LocalReminderScheduler,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                settingsRepository = settingsRepository,
                localReminderScheduler = localReminderScheduler,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun com.quran.tathbeet.domain.model.AppSettings.toUiState(): SettingsUiState =
    SettingsUiState(
        isLoading = false,
        themeMode = themeMode,
        globalNotificationsEnabled = globalNotificationsEnabled,
        motivationalMessagesEnabled = motivationalMessagesEnabled,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
    )
