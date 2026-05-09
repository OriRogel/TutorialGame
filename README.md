# The Simian Legend 🐒
*An Android Action RPG built entirely from scratch in Java.*

<img width="400" height="180" alt="Screenrecorder-2026-05-09-22-29-06-9922" src="https://github.com/user-attachments/assets/0b8c6382-4bcf-483f-85ce-15f712849c69" />

## 📖 About The Project
The Simian Legend is a single-player, story-driven RPG. Players explore a dynamic world, interact with NPCs, complete quests, and battle enemies to level up and upgrade their stats.

## ✨ Key Engineering Features
* **Custom 2D Game Engine:** Built from scratch using Android's `SurfaceView` and `Canvas` API, featuring a reliable Delta-Time based Game Loop running on a separate thread.
* **Component-Based Architecture:** Modular entity system separating logic into reusable components (Health, Stamina, Movement, Combat, Animation).
* **Advanced AI State Machine:** Enemies and NPCs feature dynamic states (Idle, Aggressive, Retreat) with vision cones and logic-driven behaviors.
* **Cloud Sync & Authentication:** Full integration with Firebase (Auth & Firestore) for multi-slot cloud saving, offline support, and progress tracking.
* **Dynamic Lighting & Physics:** Custom AABB collision detection, knockback physics, and a real-time light masking system.
* **Tiled Map Integration:** Custom parser for `.tmx` and CSV files to render multi-layered maps designed in Tiled.

## 🛠️ Tech Stack
* **Language:** Java
* **Platform:** Android SDK
* **Backend:** Firebase (Authentication, Firestore)
* **Tools:** Tiled Map Editor, Aseprite (for pixel art)

## 🚀 How to Run
1. Clone the repository: `git clone https://github.com/OriRogel/TutorialGame.git`
2. Open the project in **Android Studio**.
3. Sync Gradle and run on an Android Emulator or physical device (API 24+ recommended).

## 📄 Documentation
For an in-depth look at the game's architecture, logic, and UML diagrams (in Hebrew), please refer to the [Project Book](docs/Project_Book.pdf).
