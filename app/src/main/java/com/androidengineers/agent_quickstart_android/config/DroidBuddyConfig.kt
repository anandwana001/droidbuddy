package com.androidengineers.agent_quickstart_android.config

import com.androidengineers.agent_quickstart_android.BuildConfig

object DroidBuddyConfig {
    fun missingRequiredValues(): List<String> = buildList {
        if (!BuildConfig.AGORA_APP_ID.matches(Regex("[a-fA-F0-9]{32}"))) add("AGORA_APP_ID")
        if (!BuildConfig.AGORA_APP_CERTIFICATE.matches(Regex("[a-fA-F0-9]{32}")))
            add("AGORA_APP_CERTIFICATE")
    }

    val isConfigured: Boolean
        get() = missingRequiredValues().isEmpty()

    fun startupHelpMessage(): String? {
        val missing = missingRequiredValues()
        return if (missing.isEmpty()) null
        else
            "Add your own ${missing.joinToString(" and ")} to the ignored root local.properties file, then rebuild and install. Use the 32-character values from your Agora test project."
    }
}
