package com.androidengineers.agent_quickstart_android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DroidBuddyNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun agendaIsUsableWithoutVoiceCredentials() {
        compose.onNodeWithText("Agenda", useUnmergedTree = true).performClick()
        compose.waitUntil(10_000) {
            compose
                .onAllNodes(androidx.compose.ui.test.hasText("Your Session Plan"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Your Session Plan").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextInput("Zebra")
        compose
            .onNodeWithText("Zebra's Frontline AI Suite - Building On-Device AI Applications")
            .assertIsDisplayed()
        compose
            .onNodeWithText("Zebra's Frontline AI Suite - Building On-Device AI Applications")
            .performClick()
        compose.onNodeWithText("Save session").performClick()
        compose.onNodeWithText("Saved · Remove").assertIsDisplayed()
    }
    @Test fun voiceLanguageSelectorExplainsOptionalMurfSetup() {
        compose.onNodeWithText("English · Voice language").performClick()
        compose.onNodeWithText("Conversation language").assertIsDisplayed()
        val hindi = compose.onNodeWithText("हिन्दी · Hindi · ARES + Murf")
        hindi.assertIsDisplayed()
        if (BuildConfig.MURF_API_KEY.isBlank()) {
            hindi.assertIsNotEnabled()
            compose.onNodeWithText("Hindi requires MURF_API_KEY in local.properties, then rebuild.").assertIsDisplayed()
        }
    }
}
