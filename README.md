# RentSplit 🏠💰

RentSplit is a premium Android application designed to make roommate and household expense sharing effortless. Built with a modern, fully-native **Jetpack Compose UI**, it features a stunning glassmorphic design system, intelligent receipt scanning, and robust offline-first architecture.

---

## ✨ Features

- **Glassmorphic UI & Smooth Animations**: Sleek, immersive interfaces with custom glass card designs, progressive blurs, and interactive micro-animations (bounce clicks, custom counters).
- **OCR Receipt Scanner**: Take a picture of your shopping or utility receipts and automatically extract the total amount and title using ML Kit OCR.
- **Smart Debt Settlement**: Computes who owes whom within the household and suggests the most efficient repayment paths to minimize the number of transactions.
- **Detailed Expense History**: Track monthly spending habits, view granular breakdowns by categories, and quickly manage transactions with swipe-to-delete.
- **Offline-First Room Database**: Secure, local-only data persistence ensuring all information is kept private on your device.
- **Backup & Export**: Easily export your household data to JSON backups, CSV sheets, or formatted Markdown reports.
- **WorkManager Reminders**: Automatic background notifications to remind roommates about upcoming rent and expense due dates.

---

## 🛠️ Technology Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/compose) (100% Kotlin native)
- **Database**: [Room](https://developer.android.com/training/data-storage/room) (SQLite object mapping with Flow support)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Background Work**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Text Recognition**: [ML Kit Text Recognition (OCR)](https://developers.google.com/ml-kit/vision/text-recognition)
- **Concurrency**: Kotlin Coroutines & Flow
- **Build System**: Gradle Kotlin DSL (`.gradle.kts`)

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (Koala or newer recommended)
- **Android SDK** 34+
- **JDK 17**

### Running the App

1. Clone the repository:
   ```bash
   git clone https://github.com/YJuntang/RentSplit.git
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync and download required dependencies.
4. Run the app on your Android emulator or physical device.

---

## 📂 Project Structure

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rentsplit/
│   │   │   │   ├── data/          # Room DB, DAOs, Repositories, Preferences
│   │   │   │   ├── di/            # Hilt Modules
│   │   │   │   ├── ui/            # Compose screens, components, ViewModels, Themes
│   │   │   │   ├── util/          # OCR scanning, CSV/JSON export, Haptics
│   │   │   │   └── worker/        # Background RentReminder workers
│   │   │   └── res/               # Vector drawables, fonts, values XMLs
│   │   └── test/                  # Unit tests for ViewModels
└── build.gradle.kts               # Gradle project-level configuration
```
