# 🏋️‍♂️ GymBro - Fitness Tracker

> A comprehensive, personalized fitness tracking application with a huge exercise library, multiple workout split creation, daily progress, and volume tracker with Google Fit integration to track steps, calories, distance, and heart rate.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-8.0+-green.svg)](https://www.android.com/)

## 📖 Table of Contents

- [🌟 Features](#-features)
- [🚀 Quick Start](#-quick-start)
- [📁 Project Structure](#-project-structure)
- [🔧 Tech Stack](#-tech-stack)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

## 🌟 Features

### 📅 Calendar & Attendance Tracking
- **Color-Coded Gym Attendance**:
  - **Green**: Went to gym.
  - **Red**: Skipped (without valid excuse).
  - **Purple**: Excused (Illness, Exam, Function, Travelling).
  - **Grey**: Rest Day.
- **Intuitive Navigation**: Easy-to-use monthly view navigation.

### 💪 Workout Management
- **Custom Splits**: Create and manage weekly workout splits (up to 6 days).
- **Active Splits**: Set and maintain an active workout split tailored to your goals.
- **Exercise Library**: Access a huge library of exercises neatly categorized by muscle group.
- **Volume Tracker**: Track daily progress and workout volume efficiently.

### 🩺 Health Dashboard
- **BMI Calculator**: Quickly calculate your BMI and receive basic health advice.
- **Weight Tracker**: Log, visualize, and track your weight history over time.
- **Health Integration**: Seamless integration with Health Connect (Google Fit) to read and track steps, calories, distance, and heart rate.

### 🔔 Smart Notifications
- **Monthly Summaries**: Get a monthly summary notification at 11:30 PM on the last day of the month.

## 🚀 Quick Start

### Prerequisites
- Android Studio (Latest Version)
- Android device or emulator running Android 8.0 (Oreo) or higher
- Google Fit or another Health Connect provider installed

### Setup & Build
1. **Clone the repository**
   ```bash
   git clone https://github.com/SmeetPatil/gymbro.git
   ```
2. **Open the project** in Android Studio.
3. **Sync Gradle files** to download all necessary dependencies.
4. **Run the app** on your emulator or physical device.

*Note: For Step Counting and Health features to work, you must grant permissions to Health Connect (Google Fit).*

## 📁 Project Structure

```text
app/src/main/java/com/.../
├── data/       # Room entities, DAOs, and Database setup
├── di/         # Hilt dependency injection modules
├── ui/         # Compose screens and ViewModels
│   ├── calendar/   # Attendance tracking UI
│   ├── workouts/   # Split and exercise management UI
│   ├── health/     # BMI, Weight, Steps UI
│   └── theme/      # AMOLED Dark theme configuration
└── worker/     # Background tasks (e.g., Monthly Summary)
```

## 🔧 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material3)
- **Architecture**: MVVM with Hilt Dependency Injection
- **Database**: Room
- **Background Work**: WorkManager
- **Health Integration**: Health Connect API (Google Fit)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is open-source. Please see the LICENSE file for more details.

## 👤 Author

### Smeet Patil
- GitHub: [@SmeetPatil](https://github.com/SmeetPatil)
- LinkedIn: [in/smeet-patil](https://linkedin.com/in/smeet-patil)
- Portfolio: [smeet-patil.vercel.app](https://smeet-patil.vercel.app)
