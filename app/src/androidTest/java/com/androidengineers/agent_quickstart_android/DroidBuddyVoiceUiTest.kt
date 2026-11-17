package com.androidengineers.agent_quickstart_android

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.androidengineers.agent_quickstart_android.model.*
import com.androidengineers.agent_quickstart_android.ui.DroidBuddyVoiceScreen
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DroidBuddyVoiceUiTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun darkConversationHasVisibleStateTranscriptAndControls() {
        var ended = 0
        var muted = 0
        compose.setContent {
            DroidBuddyVoiceScreen(
                ConversationUiState(
                    inConversation = true,
                    canSendText = true,
                    agentVisualState = AgentVisualState.LISTENING,
                    transcriptHistory =
                        listOf(
                            TranscriptTurn(
                                "preview-user",
                                1,
                                null,
                                TranscriptSpeaker.USER,
                                "Which sessions cover on-device AI?",
                                TranscriptTurnStatus.END,
                                0,
                            )
                        ),
                ),
                onCollapse = {},
                onStart = {},
                onEnd = { ended++ },
                onMute = { muted++ },
                onDraft = {},
                onSend = { _, _ -> },
                onDismiss = {},
            )
        }
        compose.onNodeWithText("I’m listening.").assertIsDisplayed()
        compose.onNodeWithText("Which sessions cover on-device AI?").assertIsDisplayed()
        compose.onNodeWithContentDescription("Mute microphone").assertIsDisplayed().performClick()
        assertEquals(1, muted)
        compose.onNodeWithContentDescription("End conversation").assertIsDisplayed()
        val directory = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        if (!directory.isNullOrBlank()) {
            File(directory).mkdirs()
            File(directory, "droidbuddy-voice-preview.png").outputStream().use {
                compose
                    .onRoot()
                    .captureToImage()
                    .asAndroidBitmap()
                    .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        compose.onNodeWithText("View all").performClick()
        compose.onNodeWithText("Your conversation").assertIsDisplayed()
        compose.onNode(hasText("YOU") and hasAnyAncestor(hasTestTag("transcript-list"))).assertIsDisplayed()
        compose.onNodeWithContentDescription("Close transcript").assertIsDisplayed()
        if (!directory.isNullOrBlank()) {
            File(directory, "droidbuddy-transcript-preview.png").outputStream().use {
                InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                    .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        compose.onNodeWithContentDescription("Close transcript").performClick()
        compose.onNodeWithContentDescription("End conversation").performClick()
        assertEquals(1, ended)
    }
}
