# Kreeda-Ankana 🏟️
**The Ultimate Sports Ground & Match Management Ecosystem**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2024%2B-green.svg)](https://developer.android.com/)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore-orange.svg)](https://firebase.google.com/)

---

## 📖 Overview
**Kreeda-Ankana** is a robust Android application designed to bridge the gap between sports enthusiasts, ground managers, and competitive teams. Whether you are looking to book a local ground for a practice session or want to challenge other teams for a high-stakes match, Kreeda-Ankana provides a seamless, real-time platform to organize and play.

### 🚩 Problem Statement
Organizing local sports matches often involves fragmented communication, manual ground booking, and difficulty in finding competitive teams. Kreeda-Ankana centralizes these processes, offering a unified dashboard for bookings, team management, and match challenges.

---

## ✨ Key Features
- **🏟️ Smart Ground Booking**: Real-time slot availability, multi-sport support (Cricket, Football, etc.), and automated booking constraints.
- **🤝 Team Management**: Create teams, manage rosters, and assign captain/member roles.
- **⚔️ Challenge Board**: Post open challenges to other teams and track accepted/pending requests.
- **📊 Real-time Sync**: Hybrid data architecture using **Room DB** for offline persistence and **Firebase Firestore** for cloud synchronization.
- **🛡️ Multi-role Access**: Distinct workflows for Members, Captains, Supervisors, and Admins.
- **🔐 Secure Authentication**: Email-based signup/login with strict validation and profile management.

---

## 🛠️ Tech Stack
- **Language**: Kotlin
- **UI Framework**: XML Layouts with Material Design 3
- **Local Database**: Room DB (Persistence)
- **Cloud Database**: Firebase Firestore (Real-time Sync)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Manual Repository Pattern
- **Build System**: Gradle (Kotlin DSL)

---

## 📂 Project Structure
```text
app/src/main/java/com/example/kreedaankana/
├── data/
│   ├── db/          # Room DB entities, DAOs, and Database configuration
│   ├── firebase/    # Firestore sync logic and remote data sources
│   └── repository/  # Single source of truth (Room + Firebase abstraction)
├── ui/
│   ├── auth/        # Login and Signup fragments/logic
│   ├── booking/     # Ground booking workflow
│   ├── challenge/   # Challenge board and match-up logic
│   ├── grounds/     # Ground details and availability
│   ├── home/        # Dashboard and main navigation
│   ├── match/       # Match reporting and scoring
│   └── team/        # Team creation and management
└── utils/           # Validations, Formatters, and Constants
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or higher
- JDK 17
- Android SDK API 24+

### Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/vaishnavir05/Kreeda-Ankana-Public.git
   ```
2. **Open in Android Studio**:
   - Navigate to the project folder and select `settings.gradle.kts`.
3. **Firebase Setup**:
   - Add your `google-services.json` to the `app/` directory.
   - Enable Firestore and Authentication in your Firebase Console.
4. **Build and Sync**:
   - Click `Sync Project with Gradle Files` in Android Studio.

### Running the App
To install the debug APK on a connected device:
```bash
./gradlew installDebug
```

---

## 📸 Screenshots
| Home Dashboard | Ground Booking | Challenge Board |
| :---: | :---: | :---: |
| ![Home](screenshots/home.png) | ![Booking](screenshots/booking.png) | ![Challenges](screenshots/challenges.png) |

---

## 📜 License
This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

---

## 👥 Contributors
- **Vaishnavi R** - *Lead Developer*

---
*Developed as part of the Automated Project Evaluation program.*
