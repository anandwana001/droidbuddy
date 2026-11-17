package com.androidengineers.agent_quickstart_android.data

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class VoiceLanguageTest {
    private fun payload(language: VoiceLanguage, key: String = "") =
        Gson()
            .toJsonTree(
                DirectAgoraApi.joinPayload(
                    "channel",
                    "42",
                    "token",
                    "agenda fixture",
                    language,
                    key,
                )
            )
            .asJsonObject

    @Test
    fun englishUsesAresWithoutDeepgramOrMurf() {
        val body = payload(VoiceLanguage.ENGLISH)
        assertFalse(body.toString().contains("deepgram"))
        val props = body.getAsJsonObject("properties")
        assertEquals("ares", props.getAsJsonObject("asr")["vendor"].asString)
        assertEquals("en-US", props.getAsJsonObject("asr")["language"].asString)
        assertEquals("minimax", props.getAsJsonObject("tts")["vendor"].asString)
        assertFalse(props.toString().contains("api_key"))
    }

    @Test
    fun hindiConfiguresRecognitionTurnDetectionSpeechAndPromptTogether() {
        val body = payload(VoiceLanguage.HINDI, "test-murf-key")
        assertEquals("openai_gpt_4o_mini", body["preset"].asString)
        val props = body.getAsJsonObject("properties")
        assertEquals("hi-IN", props.getAsJsonObject("asr")["language"].asString)
        assertEquals("hi-IN", props.getAsJsonObject("turn_detection")["language"].asString)
        val tts = props.getAsJsonObject("tts")
        assertEquals("murf", tts["vendor"].asString)
        val params = tts.getAsJsonObject("params")
        assertEquals("hi-IN", params["locale"].asString)
        assertEquals("Neeta", params["voiceId"].asString)
        assertEquals("test-murf-key", params["api_key"].asString)
        assertTrue(props.getAsJsonObject("llm").toString().contains("Devanagari"))
        assertFalse(props.getAsJsonObject("llm").toString().contains("test-murf-key"))
    }

    @Test
    fun missingMurfKeyFailsBeforeStartingRtc() = runBlocking {
        val api = DirectAgoraApi("a".repeat(32), "b".repeat(32))
        try {
            api.bootstrap(VoiceLanguage.HINDI)
            fail("Expected missing key")
        } catch (error: java.io.IOException) {
            assertTrue(error.message!!.contains("MURF_API_KEY"))
        }
        assertNotNull(api.bootstrap(VoiceLanguage.ENGLISH))
    }

    @Test
    fun activeSessionLanguageCannotBeChanged() = runBlocking {
        val api = DirectAgoraApi("a".repeat(32), "b".repeat(32), murfApiKey = "test-key")
        val first = api.bootstrap(VoiceLanguage.ENGLISH)
        try {
            api.bootstrap(VoiceLanguage.HINDI)
            fail("Expected session guard")
        } catch (error: java.io.IOException) {
            assertTrue(error.message!!.contains("End"))
        }
        api.cancel(first.channel)
        assertNotEquals(first.channel, api.bootstrap(VoiceLanguage.HINDI).channel)
    }

    @Test
    fun unknownStoredLanguageDefaultsToEnglish() {
        assertEquals(VoiceLanguage.ENGLISH, VoiceLanguage.fromStored("unknown"))
        assertEquals(VoiceLanguage.HINDI, VoiceLanguage.fromStored("HINDI"))
    }
}
