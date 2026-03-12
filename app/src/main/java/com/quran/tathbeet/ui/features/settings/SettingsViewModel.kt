package com.quran.tathbeet.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.app.LocalReminderScheduler
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.domain.repository.SettingsRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.observeSettings(),
                profileRepository.observeAccounts(),
                profileRepository.observeActiveAccount(),
            ) { settings, accounts, activeAccount ->
                Triple(settings, accounts, activeAccount?.id)
            }.collectLatest { (settings, accounts, activeAccountId) ->
                val scheduleFlows = accounts.map { account ->
                    scheduleRepository.observeActiveSchedule(account.id)
                }
                val schedulesFlow = if (scheduleFlows.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(scheduleFlows) { schedules -> schedules.toList() }
                }
                schedulesFlow.collect { schedules ->
                    val profiles = accounts.mapIndexed { index, account ->
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

                    _uiState.value = SettingsUiState(
                        isLoading = false,
                        globalNotificationsEnabled = settings.globalNotificationsEnabled,
                        motivationalMessagesEnabled = settings.motivationalMessagesEnabled,
                        reminderHour = settings.reminderHour,
                        reminderMinute = settings.reminderMinute,
                        reminderOptions = ReminderTimeOption.entries.map { option ->
                            ReminderTimeOptionUiState(
                                hour = option.hour,
                                minute = option.minute,
                                label = option.label,
                            )
                        },
                        profiles = profiles,
                    )
                }
            }
        }
    }

    fun toggleGlobalNotifications() {
        val enabled = !_uiState.value.globalNotificationsEnabled
        viewModelScope.launch {
            settingsRepository.setGlobalNotificationsEnabled(enabled)
            localReminderScheduler.syncSchedules()
        }
    }

    fun toggleMotivationalMessages() {
        val enabled = !_uiState.value.motivationalMessagesEnabled
        viewModelScope.launch {
            settingsRepository.setMotivationalMessagesEnabled(enabled)
            localReminderScheduler.syncSchedules()
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
        viewModelScope.launch {
            settingsRepository.setReminderTime(hour, minute)
            localReminderScheduler.syncSchedules()
        }
    }

    fun toggleProfileNotifications(profileId: String) {
        val profile = _uiState.value.profiles.firstOrNull { item -> item.id == profileId } ?: return
        viewModelScope.launch {
            profileRepository.updateNotificationsEnabled(profileId, !profile.notificationsEnabled)
            localReminderScheduler.syncSchedules()
        }
    }
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val localReminderScheduler: LocalReminderScheduler,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                settingsRepository = settingsRepository,
                profileRepository = profileRepository,
                scheduleRepository = scheduleRepository,
                localReminderScheduler = localReminderScheduler,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

private enum class ReminderTimeOption(
    val hour: Int,
    val minute: Int,
) {
    SixThirty(18, 30),
    Seven(19, 0),
    SevenThirty(19, 30),
    Eight(20, 0),
    ;

    val label: String
        get() = "%02d:%02d".format(hour, minute)
}
