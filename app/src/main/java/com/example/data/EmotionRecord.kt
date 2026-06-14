package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emotion_records")
data class EmotionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inputText: String,
    val dominantEmotion: String,
    val confidenceScore: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val wellnessScore: Float,
    val emotionalSummary: String,
    val explanation: String,
    
    // Core Emotions Probability Scores
    val happyScore: Float,
    val sadScore: Float,
    val angryScore: Float,
    val fearScore: Float,
    val surpriseScore: Float,
    val loveScore: Float,
    val neutralScore: Float,
    val anxietyScore: Float,
    val depressionRiskScore: Float,
    val stressLevelScore: Float,
    
    // Explainable AI & Recommendations (Stored as JSON text strings)
    val influentialWordsJson: String,
    val recommendationsJson: String
)
