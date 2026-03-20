package com.quran.tathbeet.ui.features.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.sync.AuthSessionRepository
import com.quran.tathbeet.sync.AuthSessionStatus
import com.quran.tathbeet.sync.AuthSessionState
import com.quran.tathbeet.sync.CloudSyncManager
import com.quran.tathbeet.sync.SharedProfileActionResult
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val TAG = "SharedProfileVM"

class SharedProfileViewModel(
    private val selectedProfileId: String,
    private val profileRepository: ProfileRepository,
    private val authSessionRepository: AuthSessionRepository,
    private val cloudSyncManager: CloudSyncManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedProfileUiState())
    val uiState: StateFlow<SharedProfileUiState> = _uiState.asStateFlow()
    private var membersLoadJob: Job? = null

    init {
        launchUndispatched { observeUiState() }
    }

    fun enableSharing() {
        runProfileAction(
            successBanner = SharedProfileBanner.SharedEnabled,
            refreshMembers = true,
        ) { profileId ->
            cloudSyncManager.enableSharing(profileId)
        }
    }

    fun inviteEditor(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) return
        runProfileAction(
            successBanner = SharedProfileBanner.InviteSent,
            refreshMembers = true,
        ) { profileId ->
            cloudSyncManager.inviteEditor(profileId, trimmedEmail)
        }
    }

    fun removeEditor(email: String) {
        runProfileAction(
            successBanner = SharedProfileBanner.EditorRemoved,
            refreshMembers = true,
        ) { profileId ->
            cloudSyncManager.removeEditor(profileId, email)
        }
    }

    fun leaveProfile() {
        runProfileAction(
            successBanner = SharedProfileBanner.LeftProfile,
            refreshMembers = false,
        ) { profileId ->
            cloudSyncManager.leaveSharedProfile(profileId)
        }
    }

    private suspend fun observeUiState() {
        combine(
            profileRepository.observeAccounts(),
            authSessionRepository.observeSession(),
        ) { accounts, session ->
            SharedProfileInputs(
                activeProfile = accounts.firstOrNull { profile -> profile.id == selectedProfileId },
                session = session,
            )
        }
            .distinctUntilChanged()
            .collectLatest { inputs ->
                val activeProfile = inputs.activeProfile
                val session = inputs.session
                membersLoadJob?.cancel()
                membersLoadJob = null

                _uiState.value = activeProfile.toUiState(
                    session = session,
                    banner = _uiState.value.banner,
                )

                val profile = activeProfile ?: return@collectLatest
                if (!profile.requiresMembers()) {
                    return@collectLatest
                }

                val profileId = profile.id
                membersLoadJob = viewModelScope.launch {
                    val members = loadMembers(profileId, session.email)
                    if (_uiState.value.profileId == profileId) {
                        _uiState.value = _uiState.value.copy(members = members)
                    }
                }
            }
    }

    private fun runProfileAction(
        successBanner: SharedProfileBanner,
        refreshMembers: Boolean,
        action: suspend (String) -> SharedProfileActionResult,
    ) {
        val profileId = _uiState.value.profileId ?: return
        launchUndispatched {
            val result = runCatching {
                action(profileId)
            }.getOrElse { throwable ->
                Log.w(
                    TAG,
                    "profile action failed for profileId=$profileId message=${throwable.message}",
                    throwable,
                )
                _uiState.value = _uiState.value.copy(
                    banner = SharedProfileBanner.ShareUnavailable,
                )
                return@launchUndispatched
            }
            val members = if (refreshMembers) {
                loadMembers(profileId, _uiState.value.signedInEmail)
            } else {
                _uiState.value.members
            }
            _uiState.value = _uiState.value.copy(
                members = members,
                banner = result.toBanner(success = successBanner),
            )
        }
    }

    private suspend fun loadMembers(
        profileId: String,
        signedInEmail: String?,
    ): List<SharedProfileMemberUiState> = runCatching {
        cloudSyncManager.listMembers(profileId)
            .map { member ->
                SharedProfileMemberUiState(
                    email = member.email,
                    role = member.role,
                    isCurrentUser = member.email == signedInEmail,
                )
            }
            .sortedBy { member -> member.email }
    }.getOrElse { throwable ->
        Log.w(
            TAG,
            "loadMembers skipped for profileId=$profileId message=${throwable.message}",
            throwable,
        )
        emptyList()
    }

    private fun launchUndispatched(block: suspend () -> Unit) {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            block()
        }
    }
}

class SharedProfileViewModelFactory(
    private val selectedProfileId: String,
    private val profileRepository: ProfileRepository,
    private val authSessionRepository: AuthSessionRepository,
    private val cloudSyncManager: CloudSyncManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedProfileViewModel(
                selectedProfileId = selectedProfileId,
                profileRepository = profileRepository,
                authSessionRepository = authSessionRepository,
                cloudSyncManager = cloudSyncManager,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun SharedProfileActionResult.toBanner(
    success: SharedProfileBanner,
): SharedProfileBanner = when (this) {
    SharedProfileActionResult.Success -> success
    SharedProfileActionResult.EditorsMustBeRemovedFirst -> SharedProfileBanner.EditorsMustBeRemovedFirst
    SharedProfileActionResult.SignInRequired -> SharedProfileBanner.SignInRequired
    SharedProfileActionResult.SelfProfileCannotBeShared,
    SharedProfileActionResult.ProfileNotFound,
    SharedProfileActionResult.OwnerCannotLeaveWhileEditorsRemain,
    -> SharedProfileBanner.ShareUnavailable
}

private data class SharedProfileInputs(
    val activeProfile: LearnerAccount?,
    val session: AuthSessionState,
)

private fun LearnerAccount?.toUiState(
    session: AuthSessionState,
    banner: SharedProfileBanner?,
): SharedProfileUiState {
    if (this == null) {
        return SharedProfileUiState(
            isLoading = false,
            authStatus = session.status,
            signedInEmail = session.email,
            banner = banner,
        )
    }

    return SharedProfileUiState(
        isLoading = false,
        profileId = id,
        profileName = name,
        isSelfProfile = isSelfProfile,
        syncMode = syncMode,
        authStatus = session.status,
        signedInEmail = session.email,
        canEnableSharing = session.status == AuthSessionStatus.SignedIn &&
            !isSelfProfile &&
            (syncMode == ProfileSyncMode.LocalOnly || syncMode == ProfileSyncMode.SoloSynced),
        canInviteEditors = syncMode == ProfileSyncMode.SharedOwner,
        canLeaveProfile = syncMode == ProfileSyncMode.SharedEditor,
        members = emptyList(),
        banner = banner,
    )
}

private fun LearnerAccount.requiresMembers(): Boolean = when (syncMode) {
    ProfileSyncMode.SharedOwner,
    ProfileSyncMode.SharedEditor,
    -> true

    else -> false
}
