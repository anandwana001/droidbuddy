# Verification checklist

Run `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` and `python3 -m unittest discover -s scripts/tests`.

With a device connected, run `./gradlew :app:connectedDebugAndroidTest`. `RepeatRtmLoginDeviceTest` contains an opt-in live RTM check; inspect its required instrumentation argument before enabling it. Test credentials incur usage and must remain private.

Before sharing a demo:

- Check English ARES recognition, agent audio, interruption, mute, typed question and read-aloud on a physical device.
- With your own Murf key, check Hindi ARES recognition, Neeta speech, Hindi greeting and Devanagari transcript. This provider path is not verified by mock tests.
- End and restart twice; background/rotate/end; verify microphone and SDK cleanup.
- Select language, restart the app and confirm persistence. Check that active calls block changes and missing Murf key disables Hindi.
- Check microphone denial, offline agenda, no-network errors, token expiration and timeout cleanup.
- Validate official schedule source/date and bookmark overlap messages.
- Ensure no actual credentials or credential-bearing APK are tracked or publicly distributed.

UI screenshots and mock REST tests verify layout/contracts, not a live provider conversation. Official branding requires approval. A public release needs server-owned credentials, application authentication and production privacy review.
