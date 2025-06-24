# Trans Surabaya App 🚌

![Trans Surabaya App Banner](https://placehold.co/1200x400/1976D2/FFFFFF?text=Trans+Surabaya+App&font=raleway)

**A modern, user-centric mobile application for navigating the Trans Surabaya bus system. Built entirely with Kotlin and the latest Jetpack Compose toolkit, this app provides real-time bus tracking, seamless ticket booking, and a rewarding user experience.**

This project is a demonstration of modern Android development practices, showcasing a clean MVVM architecture, local data persistence with Room, with simple declarative UI.

---

## ✨ Features

* **User Authentication**: Secure login and registration system.
* **Routes Exploration**: Browse all available Trans Surabaya bus routes with detailed stop lists and estimated arrival times.
* **Simulated Bus Tracking**: A real-time (simulated) map view of bus locations, allowing users to track their ride.
* **Easy Ticket Booking**: Purchase tickets effortlessly by selecting origin and destination stops.
* **QR Code Boarding Pass**: Receive a digital ticket with a scannable QR code for quick and easy boarding.
* **Reward System**: A gamified experience where users can earn free rides after completing a certain number of trips.
* **Profile Management**: View personal travel statistics, check reward progress, and manage account settings.
* **Modern UI**: A beautiful and responsive user interface built entirely with Jetpack Compose, providing a smooth user experience.

---

## 🛠️ Tech Stack & Architecture

This project is built with a modern tech stack and follows Google's recommended architecture for Android development.

### Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/) (100% Kotlin)
* **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Architecture**: MVVM (Model-View-ViewModel)
* **Navigation**: [Jetpack Navigation for Compose](https://developer.android.com/jetpack/compose/navigation)
* **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
* **Database**: [Room](https://developer.android.com/training/data-storage/room) for local data persistence
* **Dependency Injection**: Manual (ViewModelFactory)
* **QR Code Generation**: [ZXing (Zebra Crossing)](https://github.com/zxing/zxing)

### Architecture

The application follows a clean MVVM architecture, ensuring separation of concerns and a scalable, maintainable codebase.

* **UI Layer (View)**: Composable functions (`HomeScreen`, `MapScreen`, etc.) that observe the ViewModel.
* **ViewModel Layer**: `TransSurabayaViewModel` holds and manages UI-related data and business logic. It exposes data via `State` and `Flow`.
* **Data Layer (Model)**: The `TransSurabayaRepository` is the single source of truth. It abstracts data sources, currently handling the local Room database.

---

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

* Android Studio Iguana | 2023.2.1 or later
* Java Development Kit (JDK) 11 or later

### Installation

1.  **Clone the repository**
    ```sh
    git clone https://github.com/chalvnrlv/transsurabayaapp.git
    ```
2.  **Open the project** in Android Studio.
3.  Let Gradle **sync the dependencies**. This might take a few moments.
4.  **Run the app** on an emulator or a physical Android device (minSdk 27).

---

## 📁 Project Structure

The project is organized into the following main packages:

* `com.example.transsurabayaapp`
    * `data`: Contains data models, repository, and data sources (local Room database).
        * `local`: DAOs, Entities, and the AppDatabase class for Room.
    * `ui`: All UI-related components.
        * `icon`: Custom vector icons for the app.
        * `navigation`: Navigation graph, routes, and bottom navigation setup.
        * `screens`: Each screen of the app as a Composable function.
        * `theme`: Compose theme, colors, and typography.
    * `util`: Utility classes, such as the `QRCodeGenerator`.
    * `viewmodel`: The `TransSurabayaViewModel` and its factory.

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

---

## 📧 Contact

Chalvin - chalvinreza654@gmail.com

Project Link: [https://github.com/chalvnrlv/transsurabayaapp](https://github.com/chalvnrlv/transsurabayaapp)
