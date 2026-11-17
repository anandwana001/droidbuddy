package com.androidengineers.agent_quickstart_android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.androidengineers.agent_quickstart_android.config.DroidBuddyConfig
import com.androidengineers.agent_quickstart_android.data.ConversationRepository
import com.androidengineers.agent_quickstart_android.model.ConversationUiState
import com.androidengineers.agent_quickstart_android.rtc.AgoraConversationSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("voice_preferences", 0)
    private val _language =
        MutableStateFlow(
            com.androidengineers.agent_quickstart_android.data.VoiceLanguage.fromStored(
                preferences.getString("language", null)
            )
        )
    val language = _language.asStateFlow()

    fun selectLanguage(value: com.androidengineers.agent_quickstart_android.data.VoiceLanguage) {
        val state = _uiState.value
        if (state.inConversation || state.isStarting || state.isStopping) return
        if (
            value.usesMurf &&
                com.androidengineers.agent_quickstart_android.BuildConfig.MURF_API_KEY.isBlank()
        )
            return
        _language.value = value
        preferences.edit().putString("language", value.name).apply()
    }

    private val repository = ConversationRepository()
    private val sessionManager = AgoraConversationSessionManager(application)
    private val _uiState = MutableStateFlow(ConversationUiStateMapper.freshUiState())

    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var activeAgentId: String? = null
    private var bootstrappedChannel: String? = null
    private var sessionExpiry: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.snapshot.collectLatest { snapshot ->
                _uiState.update { current ->
                    ConversationUiStateMapper.mergeSession(current, snapshot)
                }
            }
        }
    }

    fun updateMicrophonePermission(granted: Boolean) {
        _uiState.update { it.copy(microphonePermissionGranted = granted) }
    }

    fun microphoneDenied() {
        _uiState.update {
            it.copy(
                errorMessage =
                    "Microphone access is off. You can still explore the agenda. Allow the microphone in app settings to start the default voice session."
            )
        }
    }

    fun startConversation() {
        val currentState = _uiState.value
        if (currentState.isStarting || currentState.isStopping || currentState.inConversation) {
            return
        }
        if (!DroidBuddyConfig.isConfigured) {
            _uiState.update {
                it.copy(errorMessage = DroidBuddyConfig.startupHelpMessage(), warningMessage = null)
            }
            return
        }
        if (!currentState.microphonePermissionGranted) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Microphone access is required to publish your voice to the Agora channel.",
                    warningMessage = null,
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isStarting = true, errorMessage = null, warningMessage = null)
            }

            runCatching {
                    val bootstrap = repository.requestSessionBootstrap(_language.value)
                    bootstrappedChannel = bootstrap.channel
                    sessionManager.connect(bootstrap) { channel, rtcUid, rtmUserId ->
                        repository.renewTokens(
                            channel = channel,
                            rtcUid = rtcUid,
                            rtmUserId = rtmUserId,
                        )
                    }

                    val requesterRtcUid =
                        sessionManager.snapshot.value.localRtcUid.takeIf { it > 0 }?.toString()
                            ?: bootstrap.uid
                    val inviteAttempt = runCatching {
                        repository.inviteAgent(
                            channelName = bootstrap.channel,
                            requesterRtcUid = requesterRtcUid,
                        )
                    }
                    val inviteResult = inviteAttempt.getOrNull()
                    activeAgentId = inviteResult?.agentId
                    sessionManager.setActiveAgentId(activeAgentId)

                    val warning =
                        inviteAttempt.exceptionOrNull()?.message?.let { message ->
                            "The Android client joined the channel, but the server could not start the Agora agent: $message"
                        }
                            ?: if (inviteResult?.agentId == null) {
                                "The Android client joined the channel, but the server returned no Agora agent ID."
                            } else null

                    sessionExpiry?.cancel()
                    sessionExpiry =
                        viewModelScope.launch {
                            delay(14 * 60 * 1000L)
                            endConversation()
                        }
                    _uiState.update { current ->
                        ConversationUiStateMapper.mergeSession(
                            current.copy(
                                isStarting = false,
                                inConversation = true,
                                canSendText = activeAgentId != null,
                                warningMessage = warning,
                            ),
                            sessionManager.snapshot.value,
                        )
                    }
                }
                .onFailure { error ->
                    bootstrappedChannel?.let { channel ->
                        withTimeoutOrNull(8_000) {
                            runCatching { repository.cancelSession(channel) }
                        }
                    }
                    bootstrappedChannel = null
                    sessionManager.disconnect(resetSnapshot = true)
                    activeAgentId = null
                    _uiState.value =
                        ConversationUiStateMapper.freshUiState(
                            permissionGranted = _uiState.value.microphonePermissionGranted,
                            errorMessage =
                                error.message
                                    ?: "Unable to connect to Agora. Check your project configuration and internet connection.",
                        )
                }
        }
    }

    fun endConversation() {
        val currentState = _uiState.value
        if (currentState.isStopping || currentState.isStarting) {
            return
        }

        sessionExpiry?.cancel()
        sessionExpiry = null
        viewModelScope.launch {
            _uiState.update { it.copy(isStopping = true) }

            var warning =
                activeAgentId?.let { agentId ->
                    val channelName = sessionManager.snapshot.value.channelName
                    runCatching {
                            if (channelName != null) {
                                repository.stopConversation(agentId, channelName)
                            }
                        }
                        .exceptionOrNull()
                        ?.message
                        ?.let { message ->
                            "The local session ended, but the server leave request failed: $message"
                        }
                }

            if (activeAgentId == null)
                bootstrappedChannel?.let { channel ->
                    runCatching { repository.cancelSession(channel) }
                        .onFailure { warning = it.message }
                }
            bootstrappedChannel = null
            activeAgentId = null
            sessionManager.setActiveAgentId(null)
            sessionManager.disconnect(resetSnapshot = true)
            _uiState.value =
                ConversationUiStateMapper.freshUiState(
                    permissionGranted = _uiState.value.microphonePermissionGranted,
                    warningMessage = warning,
                )
        }
    }

    fun toggleMicrophone() {
        sessionManager.setMicrophoneEnabled(!_uiState.value.micRequestedEnabled)
    }

    fun updateTextDraft(text: String) {
        _uiState.update { it.copy(textDraft = text.take(2000), textActionStatus = null) }
    }

    fun sendText(speak: Boolean, append: Boolean) {
        val state = _uiState.value
        val agentId = activeAgentId ?: return
        val channel = sessionManager.snapshot.value.channelName ?: return
        val text = state.textDraft.trim()
        if (text.isEmpty() || state.isSendingText || state.isStopping || !state.inConversation)
            return
        _uiState.update { it.copy(isSendingText = true, textActionStatus = null) }
        viewModelScope.launch {
            val result = runCatching { repository.sendText(agentId, channel, text, speak, append) }
            // A reply from an ended session must not modify a newly started one.
            if (activeAgentId != agentId) return@launch
            _uiState.update {
                it.copy(
                    isSendingText = false,
                    textDraft =
                        if (result.isSuccess && it.textDraft == state.textDraft) ""
                        else it.textDraft,
                    textActionStatus =
                        if (result.isSuccess) {
                            if (speak) "Speech request accepted" else "Instruction accepted"
                        } else null,
                    errorMessage = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun clearTransientMessages() {
        _uiState.update { it.copy(errorMessage = null, warningMessage = null) }
    }

    override fun onCleared() {
        val agent = activeAgentId
        val channel = bootstrappedChannel
        sessionManager.release()
        // The ViewModel scope is cancelled at this point. A bounded cleanup request
        // gets its own short-lived job; Agora idle timeout is the process-death fallback.
        if (channel != null)
            CoroutineScope(Dispatchers.IO).launch {
                withTimeoutOrNull(8_000) {
                    runCatching {
                        if (agent != null) repository.stopConversation(agent, channel)
                        else repository.cancelSession(channel)
                    }
                }
            }
        super.onCleared()
    }
}
