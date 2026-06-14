# PRODUCTION DEPLOYMENT GUIDE
## EmotionAI Pro - Cognitive Enterprise SaaS Platform

---

### 1. Android APK Build

#### 1.1 Preparing the Release Build Configuration
Configure the signing configuration blocks securely in your `/app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE_PATH") ?: "release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

#### 1.2 Compiling the Release APK
Execute the following Gradle command from the root directory to generate the release bundle:
```bash
gradle assembleRelease
```
The output artifact is generated under:
`app/build/outputs/apk/release/app-release.apk`

---

### 2. Django Backend Production Deployment

#### 2.1 Environmental Variables (`.env` Configuration)
Create a `.env` file in `/backend` to store production environment variables securely:

```env
DJANGO_SECRET_KEY=prod-d7y3m48-g8-7y9n383-secure-production-code
DJANGO_DEBUG=False
DJANGO_ALLOWED_HOSTS=api.emotionaipro.com,yourdomain.com

DB_NAME=emotionai_db
DB_USER=emotion_user
DB_PASSWORD=YOUR_STRONG_DB_PASSWORD
DB_HOST=your-rds-postgresql-endpoint.aws.com
DB_PORT=5432

GEMINI_API_KEY=AIzaSyA_Your_Actual_Google_Gemini_Production_Key
```

#### 2.2 Installing PostgreSQL Database Engine
Install PostgreSQL on your production Ubuntu or Debian virtual machine:
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib libpq-dev -y
```

Now configure database users and settings:
```bash
sudo -i -u postgres psql
```
Within the SQL interface shell, enter the following commands:
```sql
CREATE DATABASE emotionai_db;
CREATE USER emotion_user WITH PASSWORD 'YOUR_STRONG_DB_PASSWORD';
ALTER ROLE emotion_user SET client_encoding TO 'utf8';
ALTER ROLE emotion_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE emotion_user SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE emotionai_db TO emotion_user;
\q
```

---

### 3. Docker Container Deployment

Docker Compose coordinates deployment, simplifying configuration, scaling, and database synchronization.

#### 3.1 Docker Compose Deployment Script
Spin up PostgreSQL and gunicorn by running:
```bash
docker-compose up --build -d
```

#### 3.2 Running System Migrations inside the Container
Run standard model migration and update scripts:
```bash
docker-compose exec web python manage.py makemigrations
docker-compose exec web python manage.py migrate
```

#### 3.3 Create Root Admin Credentials
Initialize superuser access to manage the system via Django Admin:
```bash
docker-compose exec web python manage.py createsuperuser
```

#### 3.4 Verification & Health Logs
Inspect container status and server output in real-time:
```bash
docker logs emotionai_web_app --tail=100
```
Navigate to `http://localhost:8000/admin` to confirm administrative screens are fully functional.
