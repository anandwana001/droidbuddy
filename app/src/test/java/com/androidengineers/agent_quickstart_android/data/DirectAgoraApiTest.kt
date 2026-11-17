package com.androidengineers.agent_quickstart_android.data

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class DirectAgoraApiTest {
    @Test
    fun repeatBootstrapRemintsTokensAndExpiredSessionGetsNewIdentity() = runBlocking {
        var now = System.currentTimeMillis() / 1000
        val direct = DirectAgoraApi("a".repeat(32), "b".repeat(32), clock = { now })
        val first = direct.bootstrap()
        val second = direct.bootstrap()
        assertEquals(first.channel, second.channel)
        assertEquals(first.uid, second.uid)
        assertNotEquals(first.rtcToken, second.rtcToken)
        assertNotEquals(first.rtmToken, second.rtmToken)
        direct.cancel(first.channel)
        val next = direct.bootstrap()
        assertNotEquals(first.channel, next.channel)
        now += 901
        assertNotEquals(next.channel, direct.bootstrap().channel)
    }

    @Test
    fun setupOnlyRequiresLocalAgoraCredentials() {
        val config = com.androidengineers.agent_quickstart_android.config.DroidBuddyConfig
        assertTrue(
            config.missingRequiredValues().all {
                it == "AGORA_APP_ID" || it == "AGORA_APP_CERTIFICATE"
            }
        )
        config.startupHelpMessage()?.let { message ->
            assertTrue(message.contains("local.properties"))
            assertFalse(message.contains("Function URL"))
        }
    }

    @Test
    fun directFlowPreservesVoiceAndTextControls() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val direct =
                DirectAgoraApi(
                    "a".repeat(32),
                    "b".repeat(32),
                    baseUrl = server.url("/v2/projects/").toString(),
                    agenda = { "Verified agenda fixture" },
                )
            val api = ConversationAgoraApi(directApi = direct)
            val bootstrap = api.requestSessionBootstrap()
            assertTrue(bootstrap.rtcToken.startsWith("007"))
            assertTrue(bootstrap.rtmToken.startsWith("007"))
            assertEquals(bootstrap.uid, bootstrap.rtmUserId)
            server.enqueue(MockResponse().setBody("""{"agent_id":"agent-1","status":"started"}"""))
            val joined = api.inviteAgent(bootstrap.channel, bootstrap.uid)
            val request = server.takeRequest()
            assertEquals("/v2/projects/${"a".repeat(32)}/join", request.path)
            assertTrue(request.getHeader("Authorization")!!.startsWith("agora token=007"))
            assertNull(request.getHeader("X-Firebase-AppCheck"))
            val props =
                JsonParser.parseString(request.body.readUtf8())
                    .asJsonObject
                    .getAsJsonObject("properties")
            assertEquals(bootstrap.channel, props["channel"].asString)
            assertEquals(bootstrap.uid, props.getAsJsonArray("remote_rtc_uids")[0].asString)
            assertEquals("rtm", props.getAsJsonObject("parameters")["data_channel"].asString)
            assertTrue(props.getAsJsonObject("llm").toString().contains("Verified agenda fixture"))
            api.inviteAgent(bootstrap.channel, bootstrap.uid)
            assertEquals(1, server.requestCount)
            server.enqueue(MockResponse().setBody("{}"))
            api.sendText(joined.agentId, bootstrap.channel, "Hello", false, false)
            val think = server.takeRequest()
            assertTrue(think.path!!.endsWith("/agents/agent-1/think"))
            assertEquals(
                "interrupt",
                JsonParser.parseString(think.body.readUtf8())
                    .asJsonObject["on_speaking_action"]
                    .asString,
            )
            server.enqueue(MockResponse().setBody("{}"))
            api.interruptAgent(joined.agentId, bootstrap.channel)
            assertTrue(server.takeRequest().path!!.endsWith("/interrupt"))
            server.enqueue(MockResponse().setBody("{}"))
            api.stopConversation(joined.agentId, bootstrap.channel)
            assertTrue(server.takeRequest().path!!.endsWith("/leave"))
            assertNotEquals(bootstrap.channel, api.requestSessionBootstrap().channel)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun clientTokenContainsRtcAndRtmButManagementAddsConvoAi() {
        val factory = AgoraDemoTokens("a".repeat(32), "b".repeat(32))
        assertTrue(factory.rtc("test", 42).startsWith("007"))
        assertTrue(factory.rtm("42").startsWith("007"))
        val token = factory.management("test", 123456)
        // Cross-language wire format: deflated AccessToken2 starts with a length-prefixed
        // signature.
        val compressed = java.util.Base64.getDecoder().decode(token.substring(3))
        val raw = java.util.zip.InflaterInputStream(compressed.inputStream()).readBytes()
        val bytes = java.nio.ByteBuffer.wrap(raw).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        val signatureLength = bytes.short.toInt() and 0xffff
        bytes.position(bytes.position() + signatureLength)
        val appLength = bytes.short.toInt() and 0xffff
        bytes.position(bytes.position() + appLength)
        bytes.int
        bytes.int
        bytes.int
        assertEquals(3, bytes.short.toInt())
        assertTrue(token.startsWith("007"))
    }
}
