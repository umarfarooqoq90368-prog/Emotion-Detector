# SYSTEM ARCHITECTURE & DATABASE ER DIAGRAM
## EmotionAI Pro - Cognitive Enterprise SaaS Platform

---

### 1. Unified System Architecture

EmotionAI Pro utilizes a decoupled, modern multi-tier architecture to separate critical workloads, enhance cloud scalability, and protect intellectual property.

```
+---------------------------------------------------------------------------------+
|                               CLIENT LAYER                                      |
|                                                                                 |
|   +-------------------------------------------------------------------------+   |
|   |                       Jetpack Compose Client App                        |   |
|   |                                                                         |   |
|   |   +-----------------------+                    +--------------------+   |   |
|   |   |     UI Components     |                    |   ViewModel State  |   |   |
|   |   |  - Auth/Register      |                    |  - Live Data Flow  |   |   |
|   |   |  - Real-time Tracker  | <=== (Data Flow) ==|  - StateFlow       |   |   |
|   |   |  - M3 Analytics Dev   |                    |  - Debounce Logs   |   |   |
|   |   +-----------------------+                    +--------------------+   |   |
|   |               ||                                         ||             |   |
|   |               \/                                         \/             |   |
|   |   +-----------------------+                    +--------------------+   |   |
|   |   |   Room Database       |                    | SaaSNetworkClient  |   |   |
|   |   |   - SQLite Cache      |                    | - Retrofit Config  |   |   |
|   |   +-----------------------+                    +--------------------+   |   |
|   +----------------------------------------------------------||-------------+   |
+--------------------------------------------------------------||-----------------+
                                                               ||
                                                       (Over OAuth / JWT)
                                                               ||
                                                               \/
+---------------------------------------------------------------------------------+
|                                CLOUD BACKEND                                    |
|                                                                                 |
|   +-------------------------------------------------------------------------+   |
|   |                      Django REST SaaS Gateway                           |   |
|   |                                                                         |   |
|   |   +-----------------------+                    +--------------------+   |   |
|   |   |      JWT Control      |                    |   Feature Views    |   |   |
|   |   |  - Token Issuance     |                    |  - Register Router |   |   |
|   |   |  - Token Refresh      |                    |  - Analyzer API    |   |   |
|   |   +-----------------------+                    +--------------------+   |   |
|   +------------------||---------------------------------------||------------+   |
+----------------------||---------------------------------------||----------------+
                       ||                                       ||
                       \/                                       \/
+-------------------------------+       +-----------------------------------------+
|      DATA STORAGE ENGINE      |       |          EXTERNAL AI PROVIDER           |
|                               |       |                                         |
|    +---------------------+    |       |         +---------------------------+   |
|    |      PostgreSQL     |    |       |         |      Google Gemini API    |   |
|    |  - User Credentials |    |       |         |  - Cognitive Analysis     |   |
|    |  - Profile States   |    |       |         |  - Server-to-Server Only  |   |
|    |  - Emotion Records  |    |       |         +---------------------------+   |
|    +---------------------+    |       |                                         |
+-------------------------------+       +-----------------------------------------+
```

---

### 2. Database Entity-Relationship Diagram (ERD)

The database schema is fully transactional, mapping accounts to individual security policies, customizable notification flags, and persistent sentiment histories.

```
  +-----------------------+                       +-----------------------------+
  |      django_user      |                       |         UserProfile         |
  +-----------------------+                       +-----------------------------+
  | PK  id          (Int) |<---+                  | PK  id               (Int)  |
  |     username   (Char) |    |                  | FK  user_id          (Int)  |
  |     email      (Char) |    |                  |     fullName        (Char)  |
  |     password   (Char) |    | (1-to-1 Profile) |     avatarUrl       (Char)  |
  |     first_name (Char) |    +------------------|     isDailyRemin   (Bool)  |
  |     last_name  (Char) |                       |     isMoodRemin    (Bool)  |
  |     is_active  (Bool) |                       |     created_at     (Date)  |
  +-----------------------+                       +-----------------------------+
              |
              | (1-to-Many Records)
              \/
  +---------------------------------------------+
  |           EmotionAnalysisRecord             |
  +---------------------------------------------+
  | PK  id                    (Int)             |
  | FK  user_id               (Int)             |
  |     inputText            (Text)             |
  |     timestamp            (Date)             |
  |     dominantEmotion      (Char)             |
  |     confidenceScore     (Float)             |
  |     wellnessScore       (Float)             |
  |     explanation          (Text)             |
  |     emotionalSummary     (Text)             |
  |     score_happy         (Float)             |
  |     score_sad           (Float)             |
  |     score_angry         (Float)             |
  |     score_fear          (Float)             |
  |     score_surprise      (Float)             |
  |     score_love          (Float)             |
  |     score_neutral       (Float)             |
  |     score_anxiety       (Float)             |
  |     score_depressionRisk(Float)             |
  |     score_stressLevel   (Float)             |
  |     influentialWordsJson (Text)             |
  |     recommendationsJson  (Text)             |
  +---------------------------------------------+
```

#### Structural Specifications
1.  **User Entity**: Managed by Django's native authentication framework. Utilizes password security digests hashed using BCrypt.
2.  **UserProfile Entity**: Extends `django_user` to store notification options and individual identifiers. Deleted cascade-style (`on_delete=models.CASCADE`) when the root account is deleted.
3.  **EmotionAnalysisRecord Entity**: Relates historical records to individual accounts. Detailed emotional scores are typed as floats, and complex lists (like lists of keywords or custom actions) are formatted as structured JSON strings directly in PostgreSQL.
