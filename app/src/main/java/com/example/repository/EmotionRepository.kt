package com.example.repository

import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class EmotionRepository(private val emotionDao: EmotionDao) {

    // --- DB Queries ---
    val allRecords: Flow<List<EmotionRecord>> = emotionDao.getAllRecords()

    fun searchRecords(query: String): Flow<List<EmotionRecord>> {
        return emotionDao.searchRecords(query)
    }

    suspend fun insertRecord(record: EmotionRecord): Long = withContext(Dispatchers.IO) {
        emotionDao.insertRecord(record)
    }

    suspend fun deleteRecord(record: EmotionRecord) = withContext(Dispatchers.IO) {
        emotionDao.deleteRecord(record)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        emotionDao.clearAllRecords()
    }

    // --- Gemini NLP Logic ---

    suspend fun analyzeTextRemote(text: String): EmotionAnalysisResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isKeyConfigured = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        val parsedResponse = if (!isKeyConfigured) {
            generateLocalHeuristicAnalysis(text, "API Key is missing or default. Activated local diagnostics fallback mode.")
        } else {
            try {
                // Define rigorous rules for JSON return
                val systemPrompt = """
                    You are the enterprise-grade Emotion Classification and Clinical NLP Engine for EmotionAI Pro.
                    Analyze the given input text to detect 10 variables:
                    1. Happy
                    2. Sad
                    3. Angry
                    4. Fear
                    5. Surprise
                    6. Love
                    7. Neutral
                    8. Anxiety
                    9. Depression Risk
                    10. Stress Level
                    
                    You must output a JSON object strictly matching this schema:
                    {
                      "dominantEmotion": "Happy" | "Sad" | "Angry" | "Fear" | "Surprise" | "Love" | "Neutral" | "Anxiety" | "Depression Risk" | "Stress Level",
                      "confidenceScore": Float,
                      "scores": {
                        "happy": Float,
                        "sad": Float,
                        "angry": Float,
                        "fear": Float,
                        "surprise": Float,
                        "love": Float,
                        "neutral": Float,
                        "anxiety": Float,
                        "depressionRisk": Float,
                        "stressLevel": Float
                      },
                      "explanation": "Detailed explanation of linguistic markers and triggers",
                      "emotionalSummary": "Proactive summary sentence",
                      "wellnessScore": Float (0.0 to 100.0),
                      "influentialWords": [
                        {
                          "word": "string",
                          "impact": Float (-1.0 to 1.0),
                          "category": "positive" | "negative" | "neutral" | "high-energy"
                        }
                      ],
                      "recommendations": [
                        {
                          "title": "string",
                          "description": "string",
                          "actionableSteps": ["string"]
                        }
                      ]
                    }
                    
                    All sub-scores under "scores" must be between 0.0 and 1.0, representing probability values.
                    "wellnessScore" must be between 0f (extremely critical emotional status) and 100f (highly constructive state).
                    Identify up to 5 influential words present in the user text, determining their positive or negative emotional impacts.
                    Return ONLY the valid raw JSON object. Do not wrap in markdown or prefix with chat explanations.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = text)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.2f
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )

                GeminiErrorHandler.runSafe {
                    val response = GeminiNetworkClient.apiService.generateContent(apiKey, request)
                    val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: throw NullPointerException("Empty response from NLP engine.")

                    val cleanJson = sanitizeResponseText(rawText)
                    parseEmotionResponse(cleanJson)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    throw e
                }
                if (e is GeminiConnectionException) {
                    throw e
                }
                val errorMessage = e.message ?: "Failed downstream connection"
                generateLocalHeuristicAnalysis(text, errorMessage)
            }
        }

        // Map and save to local SQLite Room DB for persistent search/history
        val influentialWordsAdapter = GeminiNetworkClient.jsonMapper.adapter(List::class.java)
        val recommendationsAdapter = GeminiNetworkClient.jsonMapper.adapter(List::class.java)

        val wordsJson = try {
            val jsonArray = JSONArray()
            parsedResponse.influentialWords.forEach { word ->
                val obj = JSONObject()
                obj.put("word", word.word)
                obj.put("impact", word.impact)
                obj.put("category", word.category)
                jsonArray.put(obj)
            }
            jsonArray.toString()
        } catch (e: Exception) {
            "[]"
        }

        val recsJson = try {
            val jsonArray = JSONArray()
            parsedResponse.recommendations.forEach { rec ->
                val obj = JSONObject()
                obj.put("title", rec.title)
                obj.put("description", rec.description)
                val stepsArray = JSONArray()
                rec.actionableSteps.forEach { step -> stepsArray.put(step) }
                obj.put("actionableSteps", stepsArray)
                jsonArray.put(obj)
            }
            jsonArray.toString()
        } catch (e: Exception) {
            "[]"
        }

        val dbRecord = EmotionRecord(
            inputText = text,
            dominantEmotion = parsedResponse.dominantEmotion,
            confidenceScore = parsedResponse.confidenceScore,
            wellnessScore = parsedResponse.wellnessScore,
            emotionalSummary = parsedResponse.emotionalSummary,
            explanation = parsedResponse.explanation,
            happyScore = parsedResponse.scores.happy,
            sadScore = parsedResponse.scores.sad,
            angryScore = parsedResponse.scores.angry,
            fearScore = parsedResponse.scores.fear,
            surpriseScore = parsedResponse.scores.surprise,
            loveScore = parsedResponse.scores.love,
            neutralScore = parsedResponse.scores.neutral,
            anxietyScore = parsedResponse.scores.anxiety,
            depressionRiskScore = parsedResponse.scores.depressionRisk,
            stressLevelScore = parsedResponse.scores.stressLevel,
            influentialWordsJson = wordsJson,
            recommendationsJson = recsJson
        )

        try {
            emotionDao.insertRecord(dbRecord)
        } catch (e: Exception) {
            // Silence local database insertion errors to protect downstream pipeline execution
        }
        parsedResponse
    }

    private fun sanitizeResponseText(rawText: String): String {
        var clean = rawText.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    private fun parseEmotionResponse(jsonString: String): EmotionAnalysisResponse {
        // High durability JSON mapping using standard helper to guarantee parsing
        val rootObj = JSONObject(jsonString)

        val dominantEmotion = rootObj.optString("dominantEmotion", "Neutral")
        val confidenceScore = rootObj.optDouble("confidenceScore", 0.70).toFloat()
        
        val scoresObj = rootObj.optJSONObject("scores") ?: JSONObject()
        val scores = EmotionScores(
            happy = scoresObj.optDouble("happy", 0.0).toFloat(),
            sad = scoresObj.optDouble("sad", 0.0).toFloat(),
            angry = scoresObj.optDouble("angry", 0.0).toFloat(),
            fear = scoresObj.optDouble("fear", 0.0).toFloat(),
            surprise = scoresObj.optDouble("surprise", 0.0).toFloat(),
            love = scoresObj.optDouble("love", 0.0).toFloat(),
            neutral = scoresObj.optDouble("neutral", 0.1).toFloat(),
            anxiety = scoresObj.optDouble("anxiety", 0.0).toFloat(),
            depressionRisk = scoresObj.optDouble("depressionRisk", 0.0).toFloat(),
            stressLevel = scoresObj.optDouble("stressLevel", 0.0).toFloat()
        )

        val explanation = rootObj.optString("explanation", "Reasoning generated dynamically.")
        val emotionalSummary = rootObj.optString("emotionalSummary", "Emotional context established.")
        val wellnessScore = rootObj.optDouble("wellnessScore", 50.0).toFloat()

        val influentialWordsList = ArrayList<InfluentialWord>()
        val wordsArray = rootObj.optJSONArray("influentialWords")
        if (wordsArray != null) {
            for (i in 0 until wordsArray.length()) {
                val item = wordsArray.optJSONObject(i)
                if (item != null) {
                    influentialWordsList.add(
                        InfluentialWord(
                            word = item.optString("word", ""),
                            impact = item.optDouble("impact", 0.0).toFloat(),
                            category = item.optString("category", "neutral")
                        )
                    )
                }
            }
        }

        val recommendationsList = ArrayList<WellnessRecommendation>()
        val recommendationsArray = rootObj.optJSONArray("recommendations")
        if (recommendationsArray != null) {
            for (i in 0 until recommendationsArray.length()) {
                val item = recommendationsArray.optJSONObject(i)
                if (item != null) {
                    val stepsList = ArrayList<String>()
                    val stepsArray = item.optJSONArray("actionableSteps")
                    if (stepsArray != null) {
                        for (j in 0 until stepsArray.length()) {
                            stepsList.add(stepsArray.optString(j))
                        }
                    }
                    recommendationsList.add(
                        WellnessRecommendation(
                            title = item.optString("title", "Practice Reflection"),
                            description = item.optString("description", "A proactive feedback option."),
                            actionableSteps = stepsList
                        )
                    )
                }
            }
        }

        return EmotionAnalysisResponse(
            dominantEmotion = dominantEmotion,
            confidenceScore = confidenceScore,
            scores = scores,
            explanation = explanation,
            emotionalSummary = emotionalSummary,
            wellnessScore = wellnessScore,
            influentialWords = influentialWordsList,
            recommendations = recommendationsList
        )
    }

    fun generateLocalHeuristicAnalysis(text: String, reason: String): EmotionAnalysisResponse {
        val lowercaseText = text.lowercase()
        
        // Keyword lists
        val happyWords = listOf("happy", "joy", "glad", "excite", "great", "wonderful", "smile", "laugh", "blessed", "cheerful", "thrilled", "beautiful", "good")
        val sadWords = listOf("sad", "unhappy", "cry", "grief", "sorrow", "tear", "alone", "hurt", "pain", "lonely", "grieved", "bad", "unfortunate", "broken")
        val angryWords = listOf("angry", "mad", "rage", "furious", "hate", "irritated", "annoyed", "pissed", "jealous", "offended", "resentful")
        val fearWords = listOf("fear", "scared", "afraid", "terrified", "panic", "dread", "fright", "spooked", "scary", "shaking", "horrified")
        val surpriseWords = listOf("surprise", "shock", "amazing", "woah", "unexpected", "wow", "unbelievable", "astonished", "startled")
        val loveWords = listOf("love", "adore", "affection", "passion", "fond", "care", "darling", "sweetheart", "caring", "friendship", "warmth")
        val anxietyWords = listOf("anxious", "anxiety", "worry", "nervous", "tension", "uneasy", "jittery", "restless", "apprehensive", "stressful")
        val depressionWords = listOf("depress", "hopeless", "despair", "misery", "worthless", "empty", "numb", "darkness", "useless", "suicide", "giving up")
        val stressWords = listOf("stress", "overwhelm", "burden", "exhaust", "burnout", "pressure", "tired", "busy", "hectic", "workload")

        // Count support
        fun countMatches(words: List<String>): Int {
            return words.count { lowercaseText.contains(it) }
        }

        val happyCount = countMatches(happyWords)
        val sadCount = countMatches(sadWords)
        val angryCount = countMatches(angryWords)
        val fearCount = countMatches(fearWords)
        val surpriseCount = countMatches(surpriseWords)
        val loveCount = countMatches(loveWords)
        val anxietyCount = countMatches(anxietyWords)
        val depressionCount = countMatches(depressionWords)
        val stressCount = countMatches(stressWords)

        // Find dominant candidate
        val candidates = listOf(
            "Happy" to happyCount * 1.2,
            "Sad" to sadCount * 1.1,
            "Angry" to angryCount * 1.3,
            "Fear" to fearCount * 1.1,
            "Surprise" to surpriseCount * 1.0,
            "Love" to loveCount * 1.4,
            "Anxiety" to anxietyCount * 1.25,
            "Depression Risk" to depressionCount * 1.5,
            "Stress Level" to stressCount * 1.2
        )

        val winningCandidate = candidates.maxByOrNull { it.second }
        val dominantEmotion = if (winningCandidate != null && winningCandidate.second > 0) {
            winningCandidate.first
        } else {
            "Neutral"
        }

        // Determine scores
        val totalCounts = happyCount + sadCount + angryCount + fearCount + surpriseCount + loveCount + anxietyCount + depressionCount + stressCount
        val totalNormalized = totalCounts.coerceAtLeast(1)

        val happyScore = (happyCount.toFloat() / totalNormalized * 0.8f).coerceIn(0f, 1.0f)
        val sadScore = (sadCount.toFloat() / totalNormalized * 0.8f).coerceIn(0f, 1.0f)
        val angryScore = (angryCount.toFloat() / totalNormalized * 0.8f).coerceIn(0f, 1.0f)
        val fearScore = (fearCount.toFloat() / totalNormalized * 0.8f).coerceIn(0f, 1.0f)
        val surpriseScore = (surpriseCount.toFloat() / totalNormalized * 0.8f).coerceIn(0f, 1.0f)
        val loveScore = (loveCount.toFloat() / totalNormalized * 0.8f).coerceIn(0f, 1.0f)
        val anxietyScore = (anxietyCount.toFloat() / totalNormalized * 0.8f).coerceIn(0f, 1.0f)
        val depressionRiskScore = (depressionCount.toFloat() / totalNormalized * 0.82f).coerceIn(0f, 1.0f)
        val stressLevelScore = (stressCount.toFloat() / totalNormalized * 0.8f).coerceIn(0f, 1.0f)
        val neutralScore = if (dominantEmotion == "Neutral") 0.7f else 0.15f

        val confidenceScore = if (dominantEmotion == "Neutral") 0.5f else 0.82f

        // Wellness score calculation
        val wellnessScore = when (dominantEmotion) {
            "Happy", "Love" -> 85f + (happyCount + loveCount) * 2f
            "Surprise" -> 65f
            "Neutral" -> 50f
            "Stress Level", "Anxiety" -> 45f - (stressCount + anxietyCount) * 2f
            "Angry" -> 35f - angryCount * 3f
            "Fear" -> 30f - fearCount * 3f
            "Sad" -> 25f - sadCount * 3f
            "Depression Risk" -> 15f - depressionCount * 4f
            else -> 50f
        }.coerceIn(5f, 100f)

        // Generate dynamic explanation and recommendations based on dominant emotion
        val explanationText = when (dominantEmotion) {
            "Happy" -> "The review highlights positive indicators. Linguistic cues like joy or cheerfulness indicate a strong, healthy and productive outlook on current events."
            "Love" -> "The review detects strong affectionate bonds. High occurrence of warm and caring terminology marks constructive social and personal alignment."
            "Sad" -> "The text reveals sorrowful undertones. Terms reflecting pain, grief, or loneliness signal emotional distress, suggesting a period of vulnerability."
            "Angry" -> "A heightened state of frustration is detected. Words of irritation or aggression indicate rising distress and low patience."
            "Fear" -> "Vocal markers detect deep insecurity or worry. Terms reflecting danger or panic show that you may currently feel cornered or unsafe."
            "Surprise" -> "A high degree of unexpected cognitive stimulation is observed. Cognitive processes are rapidly integrating new, unpredicted parameters."
            "Anxiety" -> "Apprehensive markers are prevalent. Recurrent expressions of uncertainty or tension suggest persistent alertness and sympathetic dominance."
            "Depression Risk" -> "Strong indicators of severe emotional deceleration. Deep feelings of hopelessness or worthlessness underscore a crucial need for soft support."
            "Stress Level" -> "Cognitive workload exceeds baseline capacity. Statements of workload pressure and physical exhaustion indicate acute pressure."
            else -> "The system detected balanced, steady markers without extreme peaks, indicating objective and grounded baseline emotional processing."
        }

        val summaryText = "Secure Offline engine processed the entry successfully [Diagnostics Fallback active]."

        // Influential words parsing
        val wordsFound = ArrayList<InfluentialWord>()
        val wordsSplit = text.split("\\s+".toRegex())
        var wordCount = 0
        for (w in wordsSplit) {
            if (wordCount >= 5) break
            val cleanW = w.replace("[^a-zA-Z]".toRegex(), "").lowercase()
            if (cleanW.length < 3) continue
            
            val isPositive = happyWords.contains(cleanW) || loveWords.contains(cleanW)
            val isNegative = sadWords.contains(cleanW) || angryWords.contains(cleanW) || fearWords.contains(cleanW) || anxietyWords.contains(cleanW) || depressionWords.contains(cleanW) || stressWords.contains(cleanW)
            
            if (isPositive) {
                wordsFound.add(InfluentialWord(cleanW, 0.75f, "positive"))
                wordCount++
            } else if (isNegative) {
                wordsFound.add(InfluentialWord(cleanW, -0.75f, "negative"))
                wordCount++
            }
        }
        if (wordsFound.isEmpty()) {
            wordsFound.add(InfluentialWord("analytical-baseline", 0.0f, "neutral"))
        }

        val recList = when (dominantEmotion) {
            "Happy", "Love" -> listOf(
                com.example.data.WellnessRecommendation("Sustain Momentum", "Share this positive atmosphere with a trusted peer or colleague.", listOf("Document what caused this joyful state.", "Engage in social sharing.", "Take time to appreciate this milestone.")),
                com.example.data.WellnessRecommendation("Mindful Gratitude", "Leverage this momentum to ground yourself in current successes.", listOf("Maintain a daily gratitude entry.", "Celebrate with a healthy treat."))
            )
            "Sad" -> listOf(
                com.example.data.WellnessRecommendation("Self-Compassion Loop", "Allow yourself to feel and release emotions without judgment.", listOf("Engage in deep, slow nasal breathing.", "Listen to a comforting ambient soundscape.", "Draft a soft written log of your feelings.")),
                com.example.data.WellnessRecommendation("Gentle Connection", "Reach out to a close friend or family member for simple grounding.", listOf("Send a brief check-in note to someone you trust.", "Walk outdoors in natural sunlight for 15 minutes."))
            )
            "Angry" -> listOf(
                com.example.data.WellnessRecommendation("Physiological Sighing", "Rapidly lower your autonomic nervous system arousal.", listOf("Take double inhales followed by one long, slow exhale.", "Perform progressive muscle relaxation in quiet room.", "Squeeze and release tension blocks.")),
                com.example.data.WellnessRecommendation("Neutral Venting", "Channel the physical charge of frustration productively.", listOf("Write a raw text stream and delete it.", "Engage in modern physical exercise."))
            )
            "Fear" -> listOf(
                com.example.data.WellnessRecommendation("Grounded Awareness", "Re-anchor your awareness in the physical present.", listOf("Deploy the 5-4-3-2-1 sensory matching drill.", "Place your feet flat on the ground and focus on the contact.", "Wrap yourself in a weighted blanket if available."))
            )
            "Surprise" -> listOf(
                com.example.data.WellnessRecommendation("Cognitive Grounding", "Integrate sudden changes step-by-step.", listOf("Sit down and log the sudden update.", "Identify things that remain stable.", "Outline immediate actionable steps."))
            )
            "Anxiety", "Stress Level" -> listOf(
                com.example.data.WellnessRecommendation("Vagus Nerve Stimulation", "Promote parasympathetic tone rapidly to calm the brain.", listOf("Splash cold water on your face.", "Perform a standard box-breathing cycle (4s in, 4s hold, 4s out, 4s hold).", "Hum a soft, low note for 30 seconds.")),
                com.example.data.WellnessRecommendation("Task De-escalation", "Mitigate cognitive overload by dividing targets.", listOf("Identify the single highest priority task.", "Postpone non-urgent targets to tomorrow.", "Log a physical paper checklist for tasks."))
            )
            "Depression Risk" -> listOf(
                com.example.data.WellnessRecommendation("Micro-Step Activation", "Begin gentle behavioral reactivation with minimal goals.", listOf("Wash your hands with warm water.", "Stand up and stretch for 10 seconds.", "Look out the window for one minute.", "Know that you are valued and are not alone."))
            )
            else -> listOf(
                com.example.data.WellnessRecommendation("Balanced Reflection", "Utilize this stable state to plan and research future targets.", listOf("Organize your desk or work area.", "Review your progress for the past week.", "Set 3 modest targets for tomorrow."))
            )
        }

        return EmotionAnalysisResponse(
            dominantEmotion = dominantEmotion,
            confidenceScore = confidenceScore,
            scores = EmotionScores(
                happy = happyScore,
                sad = sadScore,
                angry = angryScore,
                fear = fearScore,
                surprise = surpriseScore,
                love = loveScore,
                neutral = neutralScore,
                anxiety = anxietyScore,
                depressionRisk = depressionRiskScore,
                stressLevel = stressLevelScore
            ),
            explanation = explanationText,
            emotionalSummary = summaryText,
            wellnessScore = wellnessScore,
            influentialWords = wordsFound,
            recommendations = recList,
            isLocalFallback = true
        )
    }
}
