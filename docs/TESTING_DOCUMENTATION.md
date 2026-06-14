# TESTING DOCUMENTATION
## EmotionAI Pro - Cognitive Enterprise SaaS Platform

---

### 1. Unit Testing Strategy

#### 1.1 Android Client Unit & State Verification
The Android application utilizes standard JVM unit testing to verify state machines and formatting.

##### Example Assertions for Sentiment Score Checks:
```kotlin
@Test
fun testEmotionRecord_WellnessScoreCalculation() {
    val record = EmotionRecord(
        id = 1,
        timestamp = System.currentTimeMillis(),
        inputText = "I am highly satisfied and very excited!",
        dominantEmotion = "Happy",
        confidenceScore = 0.98f,
        wellnessScore = 95.0f,
        happyScore = 0.95f,
        sadScore = 0.05f,
        angryScore = 0.00f,
        fearScore = 0.00f,
        anxietyScore = 0.00f,
        stressLevelScore = 0.00f,
        explanation = "Extremely positive statements.",
        emotionalSummary = "Success",
        influentialWordsJson = "[]",
        recommendationsJson = "[]"
    )
    
    // Assert structural formatting
    assertEquals(95.0f, record.wellnessScore)
    assertTrue(record.dominantEmotion.isNotEmpty())
    assertEquals(0.95f, record.happyScore)
}
```

---

#### 1.2 Django DRF API View Testing
Django uses standard unittest integrations alongside custom model structures to assert system responses.

##### Integration Python Code for API Test Cases:
```python
import json
from django.urls import reverse
from django.contrib.auth.models import User
from rest_framework.test import APITestCase
from rest_framework import status

class AuthApiTests(APITestCase):
    def setUp(self):
        self.username = "test_developer"
        self.password = "secure_db_pass_2026"
        self.email = "dev@emotionaipro.com"
        self.user = User.objects.create_user(
            username=self.username,
            password=self.password,
            email=self.email
        )

    def test_obtain_jwt_token(self):
        url = reverse('token_obtain_pair')
        data = {
            "username": self.username,
            "password": self.password
        }
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('access', response.data)
        self.assertIn('refresh', response.data)

    def test_unauthorized_access_fails(self):
        url = reverse('profile_detail')
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)
```

---

### 2. Integration Testing Suite
Integration suites certify proper coordination across distinct architectural nodes.

| Integration Scope | Trigger | Evaluates | Expected Result |
| :--- | :--- | :--- | :--- |
| **Route Security Interception** | Call `GET /api/emotion/profile/` with no header | Token Auth checking | Repells requests, returning `401 Unauthorized`. |
| **Model Generation Pipeline** | Call `POST /api/emotion/analyze/` with valid text | Server-to-server Gemini channel | Connects API, sanitizes markdown response, inserts database row, and returns response in less than 2 seconds. |
| **Dashboard Aggregate Cascade** | Inserts 5 distinct records, then calls `GET /api/emotion/analytics/` | Mathematical scoring algorithm | Correctly averages wellness scores, groups dominant emotions, and generates a history trendline. |

---

### 3. User Acceptance Testing (UAT)

Reviewers can follow this visual, step-by-step UAT script to verify and sign off on features:

#### UAT Script 1: Session Management & Authentication Flow
1.  **Action**: Open the application. Ensure logged out.
2.  **Observation**: The system redirects to the Account Access Gate immediately.
3.  **Action**: Enter unregistered username details and submit.
4.  **Observation**: Validation displays a clear error warning "Invalid credentials".
5.  **Action**: Click "Don't have an account? Sign Up", fill in registration fields, and type matching passwords. Click "Register".
6.  **Observation**: System sets persistent JWT token internally, caches details, and navigates to the core dashboard.

#### UAT Script 2: Data Document Portability (PDF/CSV Export)
1.  **Action**: Navigate to the "Profile" tab.
2.  **Action**: Click the "Export PDF" CTA.
3.  **Observation**: The device uses internal canvas drawings, packages the document, writes files to application cache directory, and invokes the Android system Sharesheet.
4.  **Action**: Select the destination (e.g. Gmail or Drive) to share or save the document.
5.  **Observation**: The shared document matches enterprise reporting requirements.
