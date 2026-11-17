package com.androidengineers.agent_quickstart_android.rtc

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout

/** A new singleton SDK instance must not be created until the old one is released. */
internal class SdkCleanupQueue(
    private val scope: CoroutineScope,
    private val timeoutMillis: Long = 4_000,
) {
    private var pending: Deferred<Unit> = CompletableDeferred(Unit)

    @Synchronized
    fun cleanup(
        unsubscribe: suspend () -> Unit,
        logout: suspend () -> Unit,
        release: () -> Unit,
    ): Deferred<Unit> {
        val previous = pending
        return scope
            .async {
                previous.await()
                try {
                    withTimeout(timeoutMillis) {
                        runCatching { unsubscribe() }
                        logout()
                    }
                } catch (_: Exception) {
                    // A failed/timed-out logout must still clear the native singleton.
                } finally {
                    runCatching { release() }
                }
                Unit
            }
            .also { pending = it }
    }

    suspend fun awaitIdle() {
        val current = synchronized(this) { pending }
        current.await()
    }
}
