package com.androidengineers.agent_quickstart_android.rtc

import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Test

class SdkCleanupQueueTest {
    @Test
    fun nextClientWaitsForLogoutAndRelease() = runBlocking {
        val queue = SdkCleanupQueue(this)
        val order = mutableListOf<String>()
        val logoutCallback = CompletableDeferred<Unit>()
        queue.cleanup(
            { order += "unsubscribe" },
            {
                logoutCallback.await()
                order += "logout"
            },
            { order += "release" },
        )
        val next = launch {
            queue.awaitIdle()
            order += "create-next-uid"
        }
        yield()
        assertEquals(listOf("unsubscribe"), order)
        logoutCallback.complete(Unit)
        next.join()
        assertEquals(listOf("unsubscribe", "logout", "release", "create-next-uid"), order)
    }

    @Test
    fun failedLogoutStillReleasesBeforeRetry() = runBlocking {
        val queue = SdkCleanupQueue(this)
        val order = mutableListOf<String>()
        queue.cleanup({}, { throw IllegalStateException("Disconnected") }, { order += "release" })
        queue.awaitIdle()
        order += "retry"
        assertEquals(listOf("release", "retry"), order)
    }

    @Test
    fun missingCallbackCannotBlockFutureSessionsForever() = runBlocking {
        val queue = SdkCleanupQueue(this, timeoutMillis = 20)
        val order = mutableListOf<String>()
        queue.cleanup({}, { awaitCancellation() }, { order += "release" })
        queue.awaitIdle()
        order += "retry"
        assertEquals(listOf("release", "retry"), order)
    }
}
