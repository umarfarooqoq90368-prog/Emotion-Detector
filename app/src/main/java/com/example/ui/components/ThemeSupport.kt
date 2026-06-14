package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

data class EmotionDetails(
    val displayName: String,
    val emoji: String,
    val color: Color,
    val description: String
)

object ThemeSupport {

    fun getEmotionDetails(emotion: String): EmotionDetails {
        return when (emotion.trim().lowercase()) {
            "happy" -> EmotionDetails(
                "Happy", "😊", ColorHappy, 
                "Elated, optimistic, or positive mood"
            )
            "sad" -> EmotionDetails(
                "Sad", "😢", ColorSad, 
                "Melancholic, reflective, or depressed tone"
            )
            "angry" -> EmotionDetails(
                "Angry", "😡", ColorAngry, 
                "Frustrated, irritated, or high temper"
            )
            "fear" -> EmotionDetails(
                "Fear", "😱", ColorFear, 
                "Apprehensive, anxious, or threatened status"
            )
            "surprise" -> EmotionDetails(
                "Surprise", "😮", ColorSurprise, 
                "Astonished, startled, or non-expected event"
            )
            "love" -> EmotionDetails(
                "Love", "🥰", ColorLove, 
                "Affectionate, deeply warm, or empathetic"
            )
            "anxiety" -> EmotionDetails(
                "Anxiety", "🌀", ColorAnxiety, 
                "Nervous, high-stress, or future-preoccupied state"
            )
            "depression risk", "depression" -> EmotionDetails(
                "Depression Risk", "☁️", ColorDepression, 
                "Low vitality, fatigue, or persistent sadness"
            )
            "stress level", "stress" -> EmotionDetails(
                "Stress Level", "🍊", ColorStress, 
                "High mental load, demands exceed capability"
            )
            else -> EmotionDetails(
                "Neutral", "😐", ColorNeutral, 
                "Balanced, objective, or peaceful mindset"
            )
        }
    }

    // List of all supported core analysis targets
    val allTrackedEmotions = listOf(
        "Happy", "Sad", "Angry", "Fear", "Surprise", 
        "Love", "Neutral", "Anxiety", "Depression Risk", "Stress Level"
    )
}
