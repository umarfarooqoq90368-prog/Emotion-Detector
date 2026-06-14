# EmotionAI Pro - Cognitive Enterprise SaaS Platform

EmotionAI Pro is an enterprise-grade mental wellness and real-time sentiment analytics SaaS platform. The system is designed with a decoupled architecture, combining a **Jetpack Compose Android Client** with a containerized **Django REST Backend** to route natural language sentiment processing securely through Google's Gemini API on the server side.

This setup prevents client-side API key leaks, provides robust offline support with local Room caching, schedules automated push reminders, and supports high-fidelity PDF and CSV data exports.

---

## 📖 System Documentation Index

The repository contains comprehensive, production-grade documentation tailored for client deliveries, final year project (FYP) defenses, and portfolio reviews:

1.  ### [Software Requirements Specification (SRS)](/docs/SRS.md)
    Detailed functional and non-functional requirements, user class parameters, security specifications, and system interfaces.
2.  ### [System Architecture & Database ERD](/docs/ARCHITECTURE_AND_ERD.md)
    Visual representations and descriptions of the decoupled system architecture and transactional database schema.
3.  ### [API Architecture Documentation](/docs/API_DOCUMENTATION.md)
    Complete documentation of the Django backend's endpoints, request payloads, response bodies, and token schemes.
4.  ### [Production Deployment Guide](/docs/DEPLOYMENT_GUIDE.md)
    Step-by-step instructions for release builds, local VM server configuration, PostgreSQL installation, and Docker hosting.
5.  ### [Testing Documentation Suite](/docs/TESTING_DOCUMENTATION.md)
    Robust testing guidelines, including sample Android JUnit assertions, Django API unittest suites, and visual UAT walk-throughs.
6.  ### [Client User Manual](/docs/CLIENT_USER_MANUAL.md)
    An easy-to-follow guide for navigating the app, registering accounts, analyzing text, viewing dashboards, and sharing files.
7.  ### [Project Presentation Slides](/docs/PRESENTATION_SLIDES.md)
    Slide-by-slide copy and visual layouts, designed for student defense reviews or investor pitch decks.
8.  ### [Final Project Report](/docs/FINAL_PROJECT_REPORT.md)
    A formal, detailed academic summary covering the background, technical implementation, and key achievements of this release.

---

## 🛠️ Tech Stack & Key Components

### Android Client Application
*   **User Interface**: Modern declarative layouts built with **Jetpack Compose** and styled using **Material Design 3**.
*   **State Management**: Structured using **MVVM (Model-View-ViewModel)** with **StateFlow** and Coroutine tracking.
*   **Local Caching**: Local caching and seamless offline functionality powered by **Room DB**.
*   **Network Operations**: Complete network implementation using **Retrofit** and **Moshi**.
*   **Media & Graphics**: Built-in canvas objects, system notification managers, and native **FileProvider** utilities.

### Django REST SaaS Gateway
*   **Web Framework**: **Django** with **Django REST Framework (DRF)**.
*   **Access Control**: Stateless, secure sessions managed via **SimpleJWT**.
*   **Relational Database**: **PostgreSQL** relational schemas.
*   **Docker Integration**: Streamlined hosting and database configuration via **Docker Compose**.
*   **External AI Services**: Server-side integrations using the secure **Google Gemini REST API**.

---

## ⚡ Quick Start Deployment

### 1. Build and Run Backend Services (Docker Compose)
From the root `/backend` directory, run:
```bash
docker-compose up --build -d
```
Run system database migrations:
```bash
docker-compose exec web python manage.py migrate
```

### 2. Run Android Client
1.  Import `/app` in Android Studio.
2.  Configure your remote backend IP address in `SaaSNetworkClient.kt` (the system will default to a local sandbox fallback if the server is offline).
3.  Compile, install, and run on your physical test device or emulator.
