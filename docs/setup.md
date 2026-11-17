# Setup

Follow the [README](../README.md#build-your-own-demo) for the complete clone-and-run setup. Use Android Studio’s bundled JDK (21 recommended), SDK 36.1 and a microphone-capable Android 7.0+ device.

Only `AGORA_APP_ID` and `AGORA_APP_CERTIFICATE` are required, in root ignored `local.properties` or environment variables at build time. Add optional `MURF_API_KEY` for Hindi. Rebuild after changing credentials. No Firebase Functions URL, Google login, Python process or tunnel is used.

Select Voice language on Home before starting a conversation. English uses ARES + managed OpenAI/MiniMax. Hindi uses ARES + managed OpenAI + Murf Neeta. The Murf request uses Agora’s documented FALCON configuration. Hindi remains unavailable without a key; there is no silent fallback to English.

Enable Conversational AI, managed-model access and RTM for your Agora account. Usage and credits are account-dependent. Install on your phone, permit microphone access, and test a short conversation in each configured language. You can then disconnect your laptop.

These build-time credentials are extractable from the APK. Keep both credentials and the built APK private. Use a backend for any publicly distributed application.
