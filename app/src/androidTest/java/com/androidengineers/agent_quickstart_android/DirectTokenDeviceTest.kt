package com.androidengineers.agent_quickstart_android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidengineers.agent_quickstart_android.data.AgoraDemoTokens
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DirectTokenDeviceTest {
    @Test
    fun officialJavaTokenLibraryWorksOnAndroid() {
        val tokens = AgoraDemoTokens("a".repeat(32), "b".repeat(32))
        assertTrue(tokens.rtc("test-android", 42).startsWith("007"))
        assertTrue(tokens.rtm("42").startsWith("007"))
        assertTrue(tokens.management("test-android", 123456).startsWith("007"))
    }
}
