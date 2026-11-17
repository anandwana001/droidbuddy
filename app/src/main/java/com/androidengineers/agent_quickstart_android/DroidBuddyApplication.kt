package com.androidengineers.agent_quickstart_android

import android.app.Application
import com.androidengineers.agent_quickstart_android.data.DirectAgoraRuntime

class DroidBuddyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DirectAgoraRuntime.initialize(this)
    }
}
