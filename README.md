# GymBro - Fitness Tracker

GymBro is a comprehensive fitness tracking Android application built with Kotlin and Jetpack Compose. It is designed for Android 14+ and optimized for AMOLED displays (Dark Mode).

## Features

1.  **Calendar & Attendance Tracking**:
    -   Mark gym attendance with color codes:
        -   **Green**: Went to gym.
        -   **Red**: Skipped (without valid excuse).
        -   **Purple**: Excused (Illness, Exam, Function, Travelling).
        -   **Grey**: Rest Day.
    -   Monthly view navigation.

2.  **Workout Management**:
    -   Create and manage weekly workout splits (6 days).
    -   Set an active split.
    -   View a library of exercises categorized by muscle group.

3.  **Health Dashboard**:
    -   **BMI Calculator**: Calculate BMI and get basic advice.
    -   **Weight Tracker**: Log and view weight history.
    -   **Step Counter**: Integrates with Health Connect (Google Fit) to read daily steps.

4.  **Notifications**:
    -   Monthly summary notification at 11:30 PM on the last day of the month.

## Tech Stack

-   **Language**: Kotlin
-   **UI**: Jetpack Compose (Material3)
-   **Architecture**: MVVM with Hilt Dependency Injection
-   **Database**: Room
-   **Background Work**: WorkManager
-   **Health Integration**: Health Connect

## Setup & Build

1.  Open the project in **Android Studio**.
2.  Sync Gradle files.
3.  Run on an emulator or device running Android 8.0 (Oreo) or higher (Targeting Android 14).
4.  **Note**: For Step Counting to work, you must have Google Fit or another Health Connect provider installed and grant permissions.

## Project Structure

-   `data`: Room entities, DAOs, and Database setup.
-   `di`: Hilt modules.
-   `ui`: Compose screens and ViewModels.
    -   `calendar`: Attendance tracking.
    -   `workouts`: Split and exercise management.
    -   `health`: BMI, Weight, Steps.
    -   `theme`: AMOLED Dark theme configuration.
-   `worker`: Background tasks (Monthly Summary).
