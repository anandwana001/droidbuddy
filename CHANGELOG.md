# Changelog

## DroidBuddy 1.0.0

- Focused the original Agora Android quickstart on a clone-and-run Voice AI demo by Akshay Nandwana, Google Developer Expert in Android.
- Added Home, full-screen voice UI and the complete published droidcon India 2026 agenda snapshot, with offline search, details and bookmarks.
- Added saved English/Hindi language selection: Agora ARES recognition for both, managed MiniMax speech for English and optional Murf Neeta speech for Hindi.
- Preserved RTC/RTM, transcripts, interruption, metrics, text instructions and read-aloud.
- Fixed repeated-session RTM identity/token handling by serializing logout and client release.
- Removed Firebase Functions, Python hosting, alternate backend modes and unused UI experiments. Build-time developer-owned credentials are used only for private demo APKs.
- Added provider-contract, cleanup, agenda and device UI checks. Live Hindi/Murf speech requires separate account verification.

Original upstream attribution is retained in license.md.
