# SOFTWARE REQUIREMENTS SPECIFICATION (SRS)
## EmotionAI Pro - Cognitive Enterprise SaaS Platform

---

### 1. Introduction
#### 1.1 Purpose
This document presents a comprehensive software requirements specification for **EmotionAI Pro**, an enterprise-grade cognitive wellness platform. This system facilitates secure user registration, session management, real-time advanced natural language sentiment processing (NLP) powered by Google's Gemini API, comprehensive mental health diagnostics, and beautiful secure document exports (PDF/CSV).

#### 1.2 Scope
EmotionAI Pro is composed of:
1. **Android Client Application**: Powered by Jetpack Compose, Material Design 3, Coroutines, and local Room caching, delivering client-side UI, biometric animations, and responsive inputs.
2. **Django Enterprise Backend**: Implements secure user authentication (JWT), manages persistent user profiles, coordinates PostgreSQL queries, and secures Gemini API keys by running model interactions entirely on the server side.

#### 1.3 Key Definitions & Acronyms
*   **JWT (JSON Web Token)**: Cryptographically secure token used for authorizing stateless API transactions.
*   **NLP (Natural Language Processing)**: Algorithmic assessment of unstructured human text.
*   **Room**: Google's SQLite-backed local persistence engine for Android.
*   **SaaS (Software as a Service)**: On-demand cloud subscription delivery model.

---

### 2. General Description
#### 2.1 Product Perspective
EmotionAI Pro relocates text analytics workloads which were previously run on-device (via insecure direct API keys) to a secure, compartmentalized, and robust Django backend architecture. It establishes high-contrast user experiences, local compliance, real-time push-notifications, and seamless cloud connectivity.

#### 2.2 Product Functions
*   **Stateless Authentication Gateway**: User Login, Register, and Password Recovery.
*   **Server-Side Intelligence Router**: Evaluates input text through the backend's secure Gemini API channel, returning 10 distinct variables (Happy, Sad, Angry, Fear, Surprise, Love, Neutral, Anxiety, Depression Risk, and Stress Levels).
*   **Statistical Wellness Analyzer**: Graphically displays average well-being indices, daily mood frequency distributions, and interactive history line charts.
*   **Enterprise Exporters**: Generates high-fidelity PDF analysis records (built on a custom-scaled postscript canvas) and multi-column structured values CSV files.
*   **Cognitive Reminders**: Sets custom alarms for daily mental status updates and periodic checks.

#### 2.3 User Classes & Characteristics
*   **Individual Clients**: Seek private, encrypted cognitive logging and analytical wellness trends.
*   **Enterprise Supervisors**: Expect compliant, scalable infrastructure, downloadable spreadsheet matrices, and standardized health reports.

---

### 3. Functional Requirements

#### 3.1 Authentication & Profile Module (SEC-001)
*   **FR-1.1**: User registrations must require Username, Email, Password, and Full Name.
*   **FR-1.2**: Access tokens (JWT bearer header) must lapse after 7 days, necessitating quiet renewal via refresh tokens.
*   **FR-1.3**: Edit screen must permit real-time modification of User Full Name, Email Contact, and active notifications.

#### 3.2 Cognitive Natural Language Processing (NLP-002)
*   **FR-2.1**: The Android client app must not store the Gemini developer API key. All evaluation requests must pass securely to the server endpoint with a valid header.
*   **FR-2.2**: The parser must process user entries dynamically with a debounce cycle of 800ms during typing to preview confidence markers.
*   **FR-2.3**: Server replies must return an explanatory analytical text block joined with customized self-guided mental recommendations.

#### 3.3 Dashboard, Charts & Room Sync (DSH-003)
*   **FR-3.1**: The main dashboard screen must visualize historic coordinates using lightweight, highly visible native widgets.
*   **FR-3.2**: When offline, the app must fall back gracefully to a Room local database (`AppDatabase.kt`) to ensure that all historic entries remain accessible without network access.

#### 3.4 Storage & Sharing Utilities (EXP-004)
*   **FR-4.1**: PDF Generator must run asynchronously, drawing details and tables onto a custom Canvas with print padding (595x842pt) before returning a secure FileProvider URI.
*   **FR-4.2**: CSV Generator must map structural elements to the correct headers and stream characters onto local storage.
*   **FR-4.3**: Social Share triggers must load the FileProvider URI into the default system ShareSheet for dispatching to email, cloud storage, or third-party communications.

#### 3.5 System Reminders (NOT-005)
*   **FR-5.1**: Reminder scheduler must build separate notifications for "Daily Wellness Tracker" and "Mindful Check-in".
*   **FR-5.2**: Test buttons must trigger instant broadcasts for QA and visual confirmation.

---

### 4. Non-Functional Requirements

#### 4.1 Security
*   All web traffic must be encrypted over SSL/TLS (HTTPS).
*   API keys matching Google's models must dwell strictly within production environments on the remote host.
*   Password digests stored in PostgreSQL must utilize PBKDF2/BCrypt hashing.

#### 4.2 Performance
*   Debounced API triggers must execute cleanly to prevent overloading of network connections.
*   JSON response payloads must load, parse, and render within 1.5 seconds under normal bandwidth.

#### 4.3 Reliability
*   The application must fall back gracefully to Room database queries whenever network access is unavailable, displaying an informative message instead of crashing.
