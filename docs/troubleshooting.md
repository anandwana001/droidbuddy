# Troubleshooting

- **Missing credentials:** add your own App ID and Certificate in root `local.properties`, then rebuild/install. Values are not entered on the phone.
- **Hindi disabled:** add `MURF_API_KEY` and rebuild. A key without Murf streaming access or credits will still fail at the provider. The Neeta voice must support `hi-IN` on your account.
- **401/403:** verify the App ID/Certificate pair, Conversational AI access and RTM access. Check the phone’s automatic date/time and project credits. The app never prints credentials or provider response bodies.
- **Second conversation fails:** use the current build, which fully releases the RTM singleton before creating a new identity. End the prior conversation before restarting. Do not reuse a temporary RTC token manually.
- **Unresolved start/network timeout:** end the conversation. If cleanup cannot reach Agora, wait for idle timeout or stop the agent in Agora Console before retrying. Usage can continue until the remote agent terminates.
- **No microphone:** enable permission in Android app settings. Agenda browsing does not need permission. This build’s default voice connection requires microphone access even when using its text composer.
- **No audio:** check mute, speaker volume, audio route and network access; test on a physical device. Emulator loopback is not proof of real speech quality.
- **Old schedule:** refresh with the agenda importer, rebuild and reinstall. The bundled schedule is an explicitly dated snapshot.

No Firebase Functions, billing upgrade, Python host or public tunnel is required by this build. Agora/Murf usage is billed separately by their services.
