![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF)
![Node.js](https://img.shields.io/badge/Backend-Node.js-339933)
![Express.js](https://img.shields.io/badge/Framework-Express-000000)
![Auth0](https://img.shields.io/badge/Auth-Auth0-EB5424)
![Full Stack](https://img.shields.io/badge/Full--Stack-Project-28a745)
![License](https://img.shields.io/badge/License-MIT-blue)

![Main Image](/assets/Read-Me-Main.png)

# Book-Keeper

## Description

Book-Keeper is a native Android application built with Kotlin, designed to help users efficiently organize, track, and manage their book collections.

## Team

| Name               | GitHub Name   | Role                      |
| ------------------ | ------------- | ------------------------- |
| Curtis             | Curtis-Thomas | Project Management/Github |
| Hamim Ifty         | hamim-ifty    | Frontend                  |
| Glory Ozoji Ifeoma | Gloryozo      | Backend                   |

## Table of Contents

- [Tech Stack](#tech-stack)
- [Installation and Setup](#installation-and-setup)
- [Frontend](#frontend)

---

## Tech Stack

### Frontend

Kotlin

### Backend

Node.js | Express | MongoDb

# Installation and Setup

To run **Book-Keeper** locally, follow these steps:

## 1. Clone the Repository

```bash
git clone https://github.com/mobile-dev-2025/book-keeper.git
cd book-keeper
```

## 2. Set Up Android Development Environment

Ensure you have the following installed:

- Android Studio (latest version)
- JDK 17+
- Android SDK with necessary dependencies
- An emulator or a physical Android device

## 3. Open the Project in Android Studio

- Launch **Android Studio**
- Click on **"Open"** and select the cloned `book-keeper` folder

## 4. Configure Environment Variables

Create a `local.properties` file in the root of the project and add the necessary keys:

```properties
API_BASE_URL=
DATABASE_NAME=book_keeper_db
```

## 5. Build and Run the Application

- Select a device (emulator or physical device)
- Click **Run** (`Shift + F10`) or use the **Run** button in Android Studio

## 6. Set Up Local Database (Optional)

If using Room Database locally, no additional setup is needed.  
For remote database integration, update `local.properties` with the appropriate connection details.

The app should now be running on your Android device or emulator.

# Frontend

## Technology

Built with Kotlin

Auth, Login, logout - Auth0

## Screens

## Splash

![Splash Screen](/assets/screen/screen-splash.png)

## Login

![Login Screen](/assets/screen/screen-login.png)

## Home

![Home Screen](/assets/screen/screen-home.png)

## Add Book

![Add book Screen](/assets/screen/screen-add-book.png)

# Backend

## Technology

Built with Node JS and Express JS

Database is MongoDb

Hosted on Render

## Endpoints

User Creation

POST /checkUser

a two part endpoint that is given the userID from the frontend
