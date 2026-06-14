package com.example.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import java.util.concurrent.TimeUnit

// --- SaaS Server Requests & Responses ---

data class LoginRequest(
    val username: String,
    val em: String? = null,
    val password: String
)

data class LoginResponse(
    val access: String,
    val refresh: String,
    val username: String,
    val email: String? = "user@emotionaipro.com"
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val profile: ProfileInput? = null
)

data class ProfileInput(
    val fullName: String,
    val avatarUrl: String? = null
)

data class RegisterResponse(
    val message: String,
    val user_id: Int,
    val username: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class SaaSProfileResponse(
    val username: String,
    val email: String,
    val profile: ProfileDetails
)

data class ProfileDetails(
    val fullName: String,
    val avatarUrl: String,
    val isDailyReminderEnabled: Boolean,
    val isMoodReminderEnabled: Boolean
)

data class ProfileUpdateRequest(
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val isDailyReminderEnabled: Boolean? = null,
    val isMoodReminderEnabled: Boolean? = null
)

data class AnalyzeRequest(
    val text: String
)

data class SaaSAnalyticsResponse(
    val totalTracks: Int,
    val averageWellness: Float,
    val moodPercentages: Map<String, Float>,
    val wellnessTrend: List<TrendPoint>
)

data class TrendPoint(
    val id: Int,
    val emotion: String,
    val wellnessScore: Float,
    val timestamp: String
)

// --- Retrofit Core Interfaces ---

interface SaaSisApiService {
    @POST("api/token/")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/emotion/register/")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("api/emotion/forgot-password/")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): ForgotPasswordResponse

    @GET("api/emotion/profile/")
    suspend fun getProfile(@Header("Authorization") authHeader: String): SaaSProfileResponse

    @PUT("api/emotion/profile/")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Body body: ProfileUpdateRequest
    ): SaaSProfileResponse

    @POST("api/emotion/analyze/")
    suspend fun analyzeText(
        @Header("Authorization") authHeader: String,
        @Body body: AnalyzeRequest
    ): EmotionRecord // Saves and returns database format record from secured cloud

    @GET("api/emotion/analytics/")
    suspend fun getAnalytics(
        @Header("Authorization") authHeader: String
    ): SaaSAnalyticsResponse
}

// --- Client Instance Creator ---

object SaaSNetworkClient {
    // 192.168.10.4 is the local network IP mapping to the Django backend hosted on the local machine.
    private const val BASE_URL = "http://192.168.10.4:8000/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val apiService: SaaSisApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(GeminiNetworkClient.jsonMapper))
            .build()
            .create(SaaSisApiService::class.java)
    }
}
