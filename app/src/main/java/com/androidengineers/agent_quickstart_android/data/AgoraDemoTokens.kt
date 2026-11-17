package com.androidengineers.agent_quickstart_android.data

import io.agora.media.AccessToken2
import io.agora.media.RtcTokenBuilder2
import io.agora.rtm.RtmTokenBuilder2
import java.io.IOException

/** Developer-owned demo credentials only. Official Agora authentication library. */
class AgoraDemoTokens(private val appId: String, private val certificate: String) {
    init {
        require(
            appId.matches(Regex("[a-fA-F0-9]{32}")) && certificate.matches(Regex("[a-fA-F0-9]{32}"))
        ) {
            "Configure a valid Agora App ID and Certificate in local.properties."
        }
    }

    fun rtc(channel: String, uid: Int, seconds: Int = 900): String =
        required(
            RtcTokenBuilder2()
                .buildTokenWithUid(
                    appId,
                    certificate,
                    channel,
                    uid,
                    RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                    seconds,
                    seconds,
                )
        )

    fun rtm(uid: String, seconds: Int = 900): String =
        required(RtmTokenBuilder2().buildToken(appId, certificate, uid, seconds))

    fun management(channel: String, agentUid: Int): String {
        val token = AccessToken2(appId, certificate, 900)
        val rtc = AccessToken2.ServiceRtc(channel, AccessToken2.getUidStr(agentUid))
        rtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_JOIN_CHANNEL, 900)
        rtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_AUDIO_STREAM, 900)
        token.addService(rtc)
        val rtm = AccessToken2.ServiceRtm(agentUid.toString())
        rtm.addPrivilegeRtm(AccessToken2.PrivilegeRtm.PRIVILEGE_LOGIN, 900)
        token.addService(rtm)
        // Official agora-token 2.0.7 ServiceConvoAI is service type 9 with no payload.
        // Java authentication 2.1.3 exposes the generic Service constructor, so use
        // that extension point instead of reimplementing token signing or packing.
        token.addService(AccessToken2.Service(9.toShort()))
        return required(token.build())
    }

    private fun required(token: String?): String =
        token?.takeIf { it.startsWith("007") }
            ?: throw IOException(
                "Unable to generate Agora demo tokens. Check your local configuration."
            )
}
