# Architecture

One Android application module, Kotlin, Jetpack Compose and Material 3. The original package and Gradle structure remain compatible with the Agora quickstart. RTC 4.6.4 and RTM 2.3.0 carry audio and events.

`MainActivity` hosts Home, Talk and Agenda. `ConversationViewModel` owns immutable StateFlow UI state and user actions. `ConversationRepository` and `ConversationAgoraApi` delegate to the shared `DirectAgoraApi`. No Firebase or Python backend is involved.

`DirectAgoraApi` generates fresh identities and 15-minute tokens with Agora’s official authentication library, then calls REST v2 join/think/speak/interrupt/leave. A bootstrap mutex and per-session join lock prevent duplicate agents. Ambiguous starts are reconciled before retrying. Renewal is scoped to the original identity and remaining lifetime. A 14-minute client timer bounds sessions.

`AgoraConversationSessionManager` retains original audio focus, RTC, RTM, transcript assembly and metrics handling. `SdkCleanupQueue` serializes unsubscribe/logout/release before another RTM client is created; this avoids reusing the SDK singleton with a different UID on the second conversation. Late renewal cannot replace a newer session’s tokens.

Language preferences are device-local. The ViewModel blocks changes while starting, active or stopping; bootstrap binds an immutable language to the session. ARES ASR, turn detection, greeting, LLM response instruction and TTS locale agree. English uses managed MiniMax; Hindi uses Murf Neeta with an optional build-time key. Missing Murf configuration fails before RTC setup. Provider parameters follow the stable Agora v2 documentation.

The same bundled `assets/agenda.json` powers the offline agenda and the trusted schedule context in the agent prompt. Local bookmarks are not sent to the agent. Public event text is treated as data. No model tools, account/profile data or fabricated people are provided.

Direct credential signing is a private demo compromise. APK credentials are extractable. A production architecture must move certificate/key handling behind authenticated server endpoints.
