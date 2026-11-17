package com.androidengineers.agent_quickstart_android.data

/** Explicit, tested provider choices; adding locales requires checking both ASR and TTS support. */
enum class VoiceLanguage(
    val label: String,
    val locale: String,
    val greeting: String,
    val waiting: String,
) {
    ENGLISH(
        "English",
        "en-US",
        "Hi, I’m DroidBuddy. What would you like to explore?",
        "Please wait a moment.",
    ),
    HINDI(
        "हिन्दी · Hindi",
        "hi-IN",
        "नमस्ते, मैं DroidBuddy हूँ। आप क्या जानना चाहेंगे?",
        "एक क्षण रुकिए।",
    );

    val usesMurf: Boolean
        get() = this == HINDI

    companion object {
        fun fromStored(value: String?): VoiceLanguage =
            entries.firstOrNull { it.name == value } ?: ENGLISH
    }
}
