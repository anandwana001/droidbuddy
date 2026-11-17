package com.androidengineers.agent_quickstart_android.data

import com.androidengineers.agent_quickstart_android.model.AgentInviteResult
import com.androidengineers.agent_quickstart_android.model.AgoraTokenBundle
import com.androidengineers.agent_quickstart_android.model.RenewalTokens

/** App-facing voice controls. All network calls go directly to Agora REST. */
class ConversationAgoraApi(directApi: DirectAgoraApi? = null) {
    private val direct by lazy { directApi ?: DirectAgoraRuntime.api }

    suspend fun requestSessionBootstrap(
        language: VoiceLanguage = VoiceLanguage.ENGLISH
    ): AgoraTokenBundle = direct.bootstrap(language)

    suspend fun renewTokens(channel: String, rtcUid: Int, rtmUserId: String): RenewalTokens =
        direct.refresh(channel, rtcUid, rtmUserId)

    suspend fun inviteAgent(channelName: String, requesterRtcUid: String): AgentInviteResult =
        direct.join(channelName, requesterRtcUid)

    suspend fun cancelSession(channelName: String) {
        direct.cancel(channelName)
    }

    suspend fun stopConversation(agentId: String, channelName: String) {
        direct.action(agentId, channelName, "leave")
    }

    suspend fun interruptAgent(agentId: String, channelName: String) {
        direct.action(agentId, channelName, "interrupt")
    }

    suspend fun sendText(
        agentId: String,
        channelName: String,
        text: String,
        speak: Boolean,
        append: Boolean,
    ) {
        val action = if (append) "append" else "interrupt"
        val payload =
            if (speak)
                mapOf("text" to text, "priority" to action.uppercase(), "interruptable" to true)
            else
                mapOf(
                    "text" to text,
                    "on_listening_action" to action,
                    "on_thinking_action" to action,
                    "on_speaking_action" to action,
                    "interruptable" to true,
                )
        direct.action(agentId, channelName, if (speak) "speak" else "think", payload)
    }
}
