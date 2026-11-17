package com.androidengineers.agent_quickstart_android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.androidengineers.agent_quickstart_android.data.AgoraDemoTokens
import com.androidengineers.agent_quickstart_android.rtc.SdkCleanupQueue
import io.agora.rtm.*
import java.io.IOException
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Opt-in live auth regression: no microphone/audio and no agent/model session is started. */
@RunWith(AndroidJUnit4::class)
class RepeatRtmLoginDeviceTest {
    @Test
    fun newUserCanLoginAfterPreviousClientIsReleased() = runBlocking {
        assumeTrue(
            "Requires opt-in liveAgora=true",
            InstrumentationRegistry.getArguments().getString("liveAgora") == "true",
        )
        assumeTrue(
            BuildConfig.AGORA_APP_ID.length == 32 && BuildConfig.AGORA_APP_CERTIFICATE.length == 32
        )
        val factory = AgoraDemoTokens(BuildConfig.AGORA_APP_ID, BuildConfig.AGORA_APP_CERTIFICATE)
        val queue = SdkCleanupQueue(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate))
        val firstUid = 200000 + SecureRandom().nextInt(100000000)
        repeat(2) { pass ->
            queue.awaitIdle()
            val uid = (firstUid + pass).toString()
            val client =
                withContext(Dispatchers.Main.immediate) {
                    RtmClient.create(
                        RtmConfig.Builder(BuildConfig.AGORA_APP_ID, uid)
                            .useStringUserId(true)
                            .build()
                    )
                }
            try {
                withTimeout(30_000) { await { client.login(factory.rtm(uid), it) } }
            } finally {
                queue.cleanup({}, { await { client.logout(it) } }, { client.release() })
                queue.awaitIdle()
            }
        }
    }

    private suspend fun await(action: (ResultCallback<Void>) -> Unit) =
        suspendCancellableCoroutine<Unit> { continuation ->
            action(
                object : ResultCallback<Void> {
                    override fun onSuccess(result: Void?) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }

                    override fun onFailure(error: ErrorInfo) {
                        if (continuation.isActive)
                            continuation.resumeWithException(
                                IOException(
                                    "RTM test login/logout: ${error.getErrorCode()} ${error.getErrorReason()}"
                                )
                            )
                    }
                }
            )
        }
}
