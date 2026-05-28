# ChordMe 🎸

ChordMe is a premium, modern, and aesthetically polished guitar chords and lyrics companion. Engineered with complete offline caching, rich material geometry, and seamless device-level accounts integration, ChordMe is designed to be the ultimate partner for musicians practicing at home, in the studio, or off-the-grid.

---

## ✨ Key Features

- **🌅 Seamless Edge-to-Edge UI**: Designed entirely on Material 3 principles. The legacy system title bar has been removed to give you a gorgeous, immersive, screen-filling guitar-playing experience that respects modern window insets.
- **🎨 Canva-Inspired Google Account Detection**: Features a secure, professional entry overlay that queries `AccountManager` to detect google accounts on the device. Log in with a single tap, or continue as a guest for instant access.
- **📡 Resilient Offline Caching**: Complete live-connectivity detection with beautiful persistent notification banners. When network signals drop, Local state managers and caches ensure your cached songs stay readable.
- **🔊 Acoustic Resonance Companion**: In-app tools designed to help you practice pitch tuning with simulated audio frequencies and hardware-safe acoustic playback controllers.
- **🌿 Slate Geometric Theme**: Tailored design featuring a curated Material color scheme, clean display typography pairings, circular responsive account badges, and deep semantic touch-target touchpoints.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, fully declarative UI.
- **Programming Language**: [Kotlin](https://kotlinlang.org/) for a rich, fully expressive codebase.
- **Device Accounts**: Integrated with native `AccountManager` to provide frictionless Google credential retrieval with safe `Throwable` catch blocks.
- **Offline Reliability**: Leveraging structured Android `ConnectivityManager` callbacks with main-loop handler posts to ensure UI-thread safety.
- **Hardware Integrations**: Safe thread-delegated `AudioTrack` synthesizers for pitch-frequency generators.

---

## 🚀 Getting Started

1. Set up your Google Accounts on the emulator or device.
2. Build and install the APK via AI Studio or using Gradle:
   ```bash
   gradle assembleDebug
   ```
3. Open ChordMe, experience the sleek Canvas overlay, tap **"Continue with Google"**, select your account, and start strumming!
