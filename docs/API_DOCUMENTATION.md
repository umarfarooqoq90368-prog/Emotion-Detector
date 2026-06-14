# API ARCHITECTURE DOCUMENTATION
## EmotionAI Pro - Cognitive Enterprise SaaS Platform

This document describes all routes, headers, payload objects, validation guidelines, and mock client codes for integrating external platforms with the EmotionAI Pro SaaS engine.

---

### 1. General Request Standards
*   **Content-Type**: `application/json`
*   **Authentication Standard**: bearer-scheme JSON Web Tokens (`Authorization: Bearer <access_token>`)
*   **Error Scheme**: Consistent structured key-value messages.

---

### 2. Authentication Gateways

#### 2.1 Obtain Access JWT (`POST /api/token/`)
Retrieves fresh JWT keys using valid database keys.

*   **Request Payload**:
```json
{
  "username": "saas_developer",
  "password": "secure_db_pass_2026"
}
```

*   **Successful Response (`200 OK`)**:
```json
{
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIj...[truncated]",
  "access": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoyLC...[truncated]"
}
```

*   **Error Response (`401 Unauthorized`)**:
```json
{
  "detail": "No active account found with the given credentials"
}
```

---

#### 2.2 Refresh Token (`POST /api/token/refresh/`)
Obtains a new operational access token using a valid refresh token.

*   **Request Payload**:
```json
{
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90e...[truncated]"
}
```

*   **Successful Response (`200 OK`)**:
```json
{
  "access": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOj..."
}
```

---

#### 2.3 User Registration (`POST /api/emotion/register/`)
Creates a new account and profile inside the system database.

*   **Request Payload**:
```json
{
  "username": "wellness_pro",
  "email": "clinical@emotionaipro.com",
  "password": "strong_password_2026",
  "profile": {
    "fullName": "Elizabeth Blackwell",
    "avatarUrl": "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"
  }
}
```

*   **Successful Response (`201 Created`)**:
```json
{
  "message": "User registered successfully.",
  "user_id": 14,
  "username": "wellness_pro"
}
```

---

#### 2.4 Forgot Password (`POST /api/emotion/forgot-password/`)
Dispatches customized recovery codes to verify identity.

*   **Request Payload**:
```json
{
  "email": "clinical@emotionaipro.com"
}
```

*   **Successful Response (`200 OK`)**:
```json
{
  "message": "Password reset instructions have been dispatched securely to clinical@emotionaipro.com."
}
```

---

### 3. Profile & System Configuration

#### 3.1 Get Profile Detail (`GET /api/emotion/profile/`)
*   **Required Header**: `Authorization: Bearer <access_token>`
*   **Successful Response (`200 OK`)**:
```json
{
  "username": "wellness_pro",
  "email": "clinical@emotionaipro.com",
  "profile": {
    "fullName": "Elizabeth Blackwell",
    "avatarUrl": "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png",
    "isDailyReminderEnabled": true,
    "isMoodReminderEnabled": false
  }
}
```

---

#### 3.2 Update Profile Detail (`PUT /api/emotion/profile/`)
Allows partial or complete profile updates.

*   **Required Header**: `Authorization: Bearer <access_token>`
*   **Request Payload**:
```json
{
  "fullName": "Elizabeth Blackwell, MD",
  "isMoodReminderEnabled": true
}
```

*   **Successful Response (`200 OK`)**:
```json
{
  "message": "Profile updated successfully.",
  "profile": {
    "fullName": "Elizabeth Blackwell, MD",
    "avatarUrl": "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png",
    "isDailyReminderEnabled": true,
    "isMoodReminderEnabled": true
  }
}
```

---

### 4. Emotion Intelligence Services

#### 4.1 Perform Real-time Sentiment Analysis (`POST /api/emotion/analyze/`)
Runs standard text evaluation securely through server-side GenAI models.

*   **Required Header**: `Authorization: Bearer <access_token>`
*   **Request Payload**:
```json
{
  "text": "I feel quite overwhelmed by our current deployment deadlines. But I am hopeful after writing this document."
}
```

*   **Successful Response (`201 Created`)**:
```json
{
  "id": 41,
  "inputText": "I feel quite overwhelmed by our current deployment deadlines. But I am hopeful after writing this document.",
  "timestamp": "2026-06-11T11:05:00Z",
  "dominantEmotion": "Mixed / Hopeful",
  "confidenceScore": 0.88,
  "wellnessScore": 65.0,
  "explanation": "The speaker expresses classic markers of external timeline-induced stress while showing notable cognitive resilience.",
  "emotionalSummary": "High stress balanced with practical optimism.",
  "score_happy": 0.45,
  "score_sad": 0.10,
  "score_angry": 0.0,
  "score_fear": 0.15,
  "score_surprise": 0.0,
  "score_love": 0.0,
  "score_neutral": 0.30,
  "score_anxiety": 0.50,
  "score_depressionRisk": 0.05,
  "score_stressLevel": 0.70,
  "influentialWords": [
    {
      "word": "overwhelmed",
      "impact": 0.85,
      "category": "negative"
    },
    {
      "word": "hopeful",
      "impact": 0.90,
      "category": "positive"
    }
  ],
  "recommendations": [
    {
      "title": "Calibrate work intervals",
      "description": "Break complex release sequences into smaller, digestible cards.",
      "actionableSteps": [
        "Take a five-minute stretch every hour",
        "Set direct expectations"
      ]
    }
  ]
}
```

---

#### 4.2 Query Aggregate Metrics Dashboard (`GET /api/emotion/analytics/`)
Computes percentages and trends for statistical summary.

*   **Required Header**: `Authorization: Bearer <access_token>`
*   **Successful Response (`200 OK`)**:
```json
{
  "totalTracks": 3,
  "averageWellness": 78.3,
  "moodPercentages": {
    "Happy": 66.7,
    "Mixed / Hopeful": 33.3
  },
  "wellnessTrend": [
    {
      "id": 39,
      "emotion": "Happy",
      "wellnessScore": 85.0,
      "timestamp": "2026-06-10 14:00"
    },
    {
      "id": 40,
      "emotion": "Happy",
      "wellnessScore": 85.0,
      "timestamp": "2026-06-11 09:30"
    },
    {
      "id": 41,
      "emotion": "Mixed / Hopeful",
      "wellnessScore": 65.0,
      "timestamp": "2026-06-11 11:05"
    }
  ]
}
```
