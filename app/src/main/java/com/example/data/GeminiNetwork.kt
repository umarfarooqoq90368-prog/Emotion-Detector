package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request Models ---

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null
)

data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

// --- Gemini Response Models ---

data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

// --- Local Structured Response Format ---
// This represents the parsed domain response returned securely by the engine
data class EmotionAnalysisResponse(
    val dominantEmotion: String,
    val confidenceScore: Float,
    val scores: EmotionScores,
    val explanation: String,
    val emotionalSummary: String,
    val wellnessScore: Float,
    val influentialWords: List<InfluentialWord>,
    val recommendations: List<WellnessRecommendation>,
    val isLocalFallback: Boolean = false
)

data class EmotionScores(
    val happy: Float = 0f,
    val sad: Float = 0f,
    val angry: Float = 0f,
    val fear: Float = 0f,
    val surprise: Float = 0f,
    val love: Float = 0f,
    val neutral: Float = 0f,
    val anxiety: Float = 0f,
    val depressionRisk: Float = 0f,
    val stressLevel: Float = 0f
)

data class InfluentialWord(
    val word: String,
    val impact: Float, // -1.0 to 1.0 representing negative to positive impact
    val category: String // "positive", "negative", "neutral", "high-energy"
)

data class WellnessRecommendation(
    val title: String,
    val description: String,
    val actionableSteps: List<String>
)

// --- Retrofit & Moshi Setup ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiNetworkClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    // Secondary Moshi instance exposing direct JSON mapping
    val jsonMapper: Moshi get() = moshi
}

// --- Global Error Handling Wrapper & Custom Exception ---

class GeminiConnectionException(message: String, cause: Throwable? = null) : Exception(message, cause)

object GeminiErrorHandler {
    suspend fun <T> runSafe(block: suspend () -> T): T {
        try {
            return block()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: java.net.UnknownHostException) {
            throw GeminiConnectionException("Sorry, something went wrong. Please try again later.", e)
        } catch (e: java.net.ConnectException) {
            throw GeminiConnectionException("Sorry, something went wrong. Please try again later.", e)
        } catch (e: retrofit2.HttpException) {
            throw GeminiConnectionException("Sorry, something went wrong. Please try again later.", e)
        } catch (e: Exception) {
            if (e is GeminiConnectionException) throw e
            throw GeminiConnectionException("Sorry, something went wrong. Please try again later.", e)
        }
    }
}

