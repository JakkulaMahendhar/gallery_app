# 📸 Gallery App

A modern Android Gallery application built with Kotlin, Jetpack libraries, and clean architecture. This app allows users to browse images and videos stored on their device with a responsive UI and powerful media handling capabilities.

---

## ✨ Features

- 📂 Organizes and displays media into:
  - All Images
  - All Videos
  - Camera Media
  - Remaining folders dynamically
- 🔄 Supports switching between **List** and **Grid** views
- 📽️ Built-in video playback using **ExoPlayer**
- 🖼️ Efficient image loading with **Glide**
- 🚀 Smooth, asynchronous data handling with **Coroutines + Flows**

---

## 🧩 Tech Stack

| Layer         | Libraries / Tools Used                                                                 |
|---------------|-----------------------------------------------------------------------------------------|
| Architecture  | MVVM + Clean Architecture                                                              |
| DI            | [Dagger Hilt](https://dagger.dev/hilt/)                                                |
| UI            | Jetpack Compose / ViewBinding (optional)                                               |
| Media         | [Glide](https://github.com/bumptech/glide) for images, [ExoPlayer](https://exoplayer.dev/) for videos |
| Async Ops     | Kotlin Coroutines, Flows                                                               |
| Testing       | JUnit, Mockito, Robolectric                                                            |

---

## 🧪 Unit Testing

The project is well-tested with:

- ✅ **JUnit**: for basic unit testing
- 🔁 **Mockito**: for mocking dependencies like ContentResolver
- 🧪 **Robolectric**: to run Android-specific tests in JVM

---

## 📱 Screens & UX

- **Home Screen** loads and displays:
  - 📸 All Images
  - 📹 All Videos
  - 📁 Camera folder
  - 📁 Other folders dynamically fetched
- Users can toggle between **Grid** or **List** views for better accessibility.

---

## 🛠️ Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/JakkulaMahendhar/gallery_app.git
