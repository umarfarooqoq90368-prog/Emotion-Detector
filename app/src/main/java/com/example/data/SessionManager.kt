package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("emotion_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_DAILY_REMIN_ENABLED = "daily_reminders_enabled"
        private const val KEY_MOOD_REMIN_ENABLED = "mood_reminders_enabled"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_AUTO_ANALYZE_ENABLED = "auto_analyze_enabled"
    }

    var isAutoAnalyzeEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_ANALYZE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_ANALYZE_ENABLED, value).apply()

    var jwtToken: String?
        get() = prefs.getString(KEY_JWT_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_JWT_TOKEN, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, "SaaS Practitioner")
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var email: String?
        get() = prefs.getString(KEY_EMAIL, "user@emotionaipro.com")
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var fullName: String?
        get() = prefs.getString(KEY_FULL_NAME, "Enterprise Member")
        set(value) = prefs.edit().putString(KEY_FULL_NAME, value).apply()

    var avatarUrl: String?
        get() = prefs.getString(KEY_AVATAR_URL, "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png")
        set(value) = prefs.edit().putString(KEY_AVATAR_URL, value).apply()

    var isDailyReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_REMIN_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_REMIN_ENABLED, value).apply()

    var isMoodReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_MOOD_REMIN_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MOOD_REMIN_ENABLED, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    fun saveSession(token: String, user: String, mail: String) {
        jwtToken = token
        username = user
        email = mail
        isLoggedIn = true
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
