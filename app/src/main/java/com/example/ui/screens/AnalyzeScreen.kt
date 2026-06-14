package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmotionAnalysisResponse
import com.example.data.InfluentialWord
import com.example.ui.components.GlassCard
import com.example.ui.components.ThemeSupport
import com.example.ui.theme.*
import com.example.viewmodel.EmotionUiState
import com.example.viewmodel.EmotionViewModel
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyzeScreen(
    viewModel: EmotionViewModel,
    innerPadding: PaddingValues
) {
    val inputText by viewModel.inputText.collectAsState()
    val isRealTimeEnabled by viewModel.isRealTimeEnabled.collectAsState()
    val isAutoAnalyzeEnabled by viewModel.isAutoAnalyzeEnabled.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var voiceErrorMessage by remember { mutableStateOf<String?>(null) }
    var rmsDb by remember { mutableStateOf(0f) }

    var recordingSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(isListening) {
        if (isListening) {
            recordingSeconds = 0
            while (true) {
                kotlinx.coroutines.delay(1000L)
                recordingSeconds++
            }
        }
    }

    val formattedTimer = remember(recordingSeconds) {
        val mins = recordingSeconds / 60
        val secs = recordingSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    val speechRecognizerHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onReadyForSpeech = {
                isListening = true
                voiceErrorMessage = null
            },
            onBeginningOfSpeech = {
                isListening = true
                voiceErrorMessage = null
            },
            onRmsChanged = { rmsDb = it },
            onPartialResults = { liveText ->
                viewModel.updateInputText(liveText)
            },
            onResults = { spokenText ->
                viewModel.updateInputText(spokenText)
                isListening = false
                rmsDb = 0f
                if (viewModel.isAutoAnalyzeEnabled.value) {
                    viewModel.analyzeCurrentText()
                }
            },
            onError = { errorText ->
                if (errorText == "Permission denied.") {
                    voiceErrorMessage = "Microphone access denied. Please grant RECORD_AUDIO permission in App Settings."
                } else if (errorText == "Network unavailable.") {
                    voiceErrorMessage = "Network connection issue. Please check your internet connection."
                } else {
                    voiceErrorMessage = errorText
                }
                isListening = false
                rmsDb = 0f
            },
            onEndOfSpeech = {
                isListening = false
                rmsDb = 0f
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizerHelper.destroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                speechRecognizerHelper.startListening()
            } else {
                voiceErrorMessage = "Microphone access denied. Please grant RECORD_AUDIO permission in App Settings."
            }
        }
    )

    val handleMicClick = {
        if (isListening) {
            speechRecognizerHelper.stopListening()
            isListening = false
        } else {
            voiceErrorMessage = null
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                speechRecognizerHelper.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
                text = "EMOTION ENGINE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cognitive NLP Lab",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // --- Text Input Section ---
        var isFocused by remember { mutableStateOf(false) }
        val borderGlowTransition = rememberInfiniteTransition(label = "Focused Border Glow")
        val borderBrush = if (isFocused) {
            Brush.sweepGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.primary
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            )
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFocused) {
                        Modifier.border(
                            width = 1.5.dp,
                            brush = borderBrush,
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                ),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                text = "Enter raw text to interpret emotional spectrum:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .onFocusChanged { isFocused = it.isFocused }
                        .testTag("text_input_field"),
                    placeholder = {
                        Text(
                            "Type some thoughts... (e.g. I have been feeling slightly overwhelmed by deadlines, but staying hopeful because our team is amazing and supportive.)",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    },
                    trailingIcon = {
                        // Dummy spacer to reserve space so text doesn't overlap the floating MicrophoneButton
                        Spacer(modifier = Modifier.width(52.dp))
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                )

                MicrophoneButton(
                    isListening = isListening,
                    rmsDb = rmsDb,
                    onClick = handleMicClick,
                    modifier = Modifier.padding(bottom = 6.dp, end = 6.dp)
                )
            }

            if (isListening) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .testTag("listening_status_indicator"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val dotTransition = rememberInfiniteTransition(label = "dot_pulse")
                                val dotAlpha by dotTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, easing = EaseInOutSine),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "dot_alpha"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .graphicsLayer { alpha = dotAlpha }
                                        .background(Color(0xFF00FF88), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Listening...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                            Text(
                                                text = formattedTimer,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Speak now. English and Urdu are fully supported.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            
                            Button(
                                onClick = {
                                    speechRecognizerHelper.stopListening()
                                    isListening = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Done", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Real-time voice amplitude meter (bar chart format)
                        VoiceAmplitudeMeter(
                            rmsDb = rmsDb,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            if (voiceErrorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .testTag("voice_error_banner"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.04f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(0.85f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Voice Input Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = voiceErrorMessage ?: "Voice input error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { voiceErrorMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Dismiss error",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                 }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Toggle Real-Time live prediction ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.setRealTimeEnabled(!isRealTimeEnabled) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Real-Time NLP",
                        tint = if (isRealTimeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Real-Time Direct Predictions",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Analyze syntactical context while you type",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Switch(
                    checked = isRealTimeEnabled,
                    onCheckedChange = { viewModel.setRealTimeEnabled(it) },
                    modifier = Modifier.testTag("real_time_switch")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // --- Toggle Auto-Analyze After Voice Input ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.setAutoAnalyzeEnabled(!isAutoAnalyzeEnabled) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Auto-Analyze Voice Input",
                        tint = if (isAutoAnalyzeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Auto-Analyze After Voice Input",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Instantly trigger diagnostic analysis upon speaking",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Switch(
                    checked = isAutoAnalyzeEnabled,
                    onCheckedChange = { viewModel.setAutoAnalyzeEnabled(it) },
                    modifier = Modifier.testTag("auto_analyze_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.updateInputText("") },
                    modifier = Modifier.weight(0.35f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }

                Button(
                    onClick = { viewModel.analyzeCurrentText() },
                    modifier = Modifier
                        .weight(0.65f)
                        .testTag("analyze_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Analyze")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analyze Spectrum", fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Result presentation layer ---
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
            },
            label = "UI State transitions"
        ) { state ->
            when (state) {
                is EmotionUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ready",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Awaiting Linguistic Core input",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Enter text above or toggle live feedback to begin deep learning diagnostics.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }
                is EmotionUiState.Loading -> {
                    CyberScanningIndicator()
                }
                is EmotionUiState.Success -> {
                    ResultLayout(prediction = state.result, sourceText = inputText)
                }
                is EmotionUiState.Error -> {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prediction_error_card"),
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                        backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Analysis Failed",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Sorry, something went wrong. Please try again later.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.analyzeCurrentText() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("api_error_retry_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Retry",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultLayout(prediction: EmotionAnalysisResponse, sourceText: String) {
    val details = ThemeSupport.getEmotionDetails(prediction.dominantEmotion)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {


        // --- Core Status Deck ---
        StaggeredCard(delayMillis = 100) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = details.color.copy(alpha = 0.3f),
                backgroundColor = details.color.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(details.color.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, details.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(details.emoji, fontSize = 28.sp)
                        }
                        Column {
                            Text(
                                text = "PRIMARY EMOTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = details.color,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = details.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${(prediction.confidenceScore * 100).toInt()}% Prediction Accuracy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Wellness Score Gauge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val animatedWellnessProgress by animateFloatAsState(
                                targetValue = prediction.wellnessScore / 100f,
                                animationSpec = tween(1200, easing = EaseOutBack),
                                label = "wellness_arc_anim"
                            )
                            CircularProgressIndicator(
                                progress = { animatedWellnessProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = if (prediction.wellnessScore >= 65) ColorAnxiety else if (prediction.wellnessScore >= 40) ColorSurprise else ColorAngry,
                                strokeWidth = 5.dp,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                            Text(
                                text = "${prediction.wellnessScore.toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "WELLNESS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))

                // Proactive dynamic summary line or mental tone alert
                Text(
                    text = prediction.emotionalSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // --- Explainable AI (SHAP & Diagnostics Explanation) ---
        StaggeredCard(delayMillis = 250) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "EXPLAINABLE AI DIAGNOSTICS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "System Reasoning:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TypewriterText(
                    text = prediction.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    delayMillis = 8L
                )

                if (prediction.influentialWords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Feature Importance (SHAP Token Weights):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Linguistic terms matching active triggers modeled inside the neural layers:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Render highlighting tokens row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                    prediction.influentialWords.forEach { item ->
                        val itemColor = when (item.category.lowercase()) {
                            "positive" -> Color(0xFF10B981) // Green
                            "negative" -> Color(0xFFEF4444) // Red
                            "high-energy" -> Color(0xFFF97316) // Orange
                            else -> Color(0xFF6B7280) // Grey
                        }
                        
                        Row(
                            modifier = Modifier
                                .background(itemColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .border(1.dp, itemColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.word,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.impact >= 0) "+${(item.impact * 100).toInt()}%" else "${(item.impact * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = itemColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }

        // --- Emotion Probability Distribution ---
        StaggeredCard(delayMillis = 400) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NLP PROBABILITY GAUGES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Map all scores to beautiful list
                val sortedList = listOf(
                    Pair("Happy", prediction.scores.happy),
                    Pair("Sad", prediction.scores.sad),
                    Pair("Angry", prediction.scores.angry),
                    Pair("Fear", prediction.scores.fear),
                    Pair("Surprise", prediction.scores.surprise),
                    Pair("Love", prediction.scores.love),
                    Pair("Neutral", prediction.scores.neutral),
                    Pair("Anxiety", prediction.scores.anxiety),
                    Pair("Depression Risk", prediction.scores.depressionRisk),
                    Pair("Stress Level", prediction.scores.stressLevel)
                ).sortedByDescending { it.second }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sortedList.forEach { (emotion, score) ->
                        val emotDetails = ThemeSupport.getEmotionDetails(emotion)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(emotDetails.emoji, modifier = Modifier.padding(end = 6.dp))
                                    Text(
                                        text = emotion,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${(score * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = emotDetails.color
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            ) {
                                val animatedBarFill by animateFloatAsState(
                                    targetValue = score.coerceIn(0f, 1f),
                                    animationSpec = tween(1100, easing = EaseOutCubic),
                                    label = "bar_gauge_$emotion"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = animatedBarFill)
                                        .clip(CircleShape)
                                        .background(emotDetails.color)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Custom Interactive Recommendations ---
        if (prediction.recommendations.isNotEmpty()) {
            StaggeredCard(delayMillis = 550) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CLINICAL ACTION PLAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )

                    prediction.recommendations.forEach { recommendation ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                        .padding(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Rec",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = recommendation.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = recommendation.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    if (recommendation.actionableSteps.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            recommendation.actionableSteps.forEach { step ->
                                                Row(
                                                    verticalAlignment = Alignment.Top,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        "•",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Text(
                                                        text = step,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaggeredCard(
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        isVisible = true
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                )
    ) {
        content()
    }
}

@Composable
fun TypewriterText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    delayMillis: Long = 10L
) {
    var textToDisplay by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        textToDisplay = ""
        for (i in text.indices) {
            textToDisplay += text[i]
            delay(delayMillis)
        }
    }
    Text(
        text = textToDisplay,
        style = style,
        color = color,
        modifier = modifier,
        lineHeight = style.lineHeight
    )
}

@Composable
fun CyberScanningIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "Radar scan line")
    
    val sweepYFactor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Sweep scanner position"
    )
    
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scanner neon intensity"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(24.dp),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.TopStart
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val columns = 16
                    val rows = 8
                    val colWidth = size.width / columns
                    val rowHeight = size.height / rows
                    
                    for (i in 0..rows) {
                        drawLine(
                            color = PrimaryDark.copy(alpha = 0.04f),
                            start = Offset(0f, i * rowHeight),
                            end = Offset(size.width, i * rowHeight),
                            strokeWidth = 1f
                        )
                    }
                    for (i in 0..columns) {
                        drawLine(
                            color = PrimaryDark.copy(alpha = 0.04f),
                            start = Offset(i * colWidth, 0f),
                            end = Offset(i * colWidth, size.height),
                            strokeWidth = 1f
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .graphicsLayer {
                            translationY = sweepYFactor * 127.dp.toPx()
                        }
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PrimaryDark,
                                    SecondaryDark,
                                    PrimaryDark,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        .graphicsLayer {
                            translationY = (sweepYFactor * 127.dp.toPx() - 35.dp.toPx())
                        }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PrimaryDark.copy(alpha = 0.12f * glowIntensity),
                                    PrimaryDark.copy(alpha = 0.25f * glowIntensity)
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Scanning",
                            tint = PrimaryDark.copy(alpha = glowIntensity),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "EXTRACTING LINGUISTIC SEMANTICS...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark.copy(alpha = glowIntensity),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Running Cognitive ML Diagnostics...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Evaluating latent semantic representations, sentiment vector fields, and psychological markers",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

class SpeechRecognizerHelper(
    private val context: Context,
    private val onReadyForSpeech: () -> Unit,
    private val onBeginningOfSpeech: () -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onPartialResults: (String) -> Unit,
    private val onResults: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onEndOfSpeech: () -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    
    // State indicators
    private var isActiveSession = false
    private var lastActiveTime: Long = 0L
    private val silenceTimeoutMs = 4000L // 4 seconds of complete silence
    
    // Accumulators for stitched sentences
    private var finalizedText = ""
    private var currentPartialText = ""

    private val silenceCheckRunnable = object : Runnable {
        override fun run() {
            if (!isActiveSession) return
            val elapsed = System.currentTimeMillis() - lastActiveTime
            if (elapsed >= silenceTimeoutMs) {
                // Period of complete silence reached. Stop the session!
                stopSession()
            } else {
                mainHandler.postDelayed(this, 100)
            }
        }
    }

    fun startListening() {
        val appCtx = context.applicationContext
        mainHandler.post {
            try {
                val isAvailable = try {
                    SpeechRecognizer.isRecognitionAvailable(context) || SpeechRecognizer.isRecognitionAvailable(appCtx)
                } catch (e: Throwable) {
                    false
                }

                if (!isAvailable) {
                    onError("Speech recognition is not available or disabled on this device.")
                    return@post
                }

                // Initialize a fresh new session
                isActiveSession = true
                finalizedText = ""
                currentPartialText = ""
                lastActiveTime = System.currentTimeMillis()

                mainHandler.removeCallbacks(silenceCheckRunnable)
                mainHandler.postDelayed(silenceCheckRunnable, 100)

                startRecognizerInternal()
            } catch (t: Throwable) {
                onError("Failed to start speech service: ${t.localizedMessage}")
            }
        }
    }

    private fun startRecognizerInternal() {
        if (!isActiveSession) return
        
        mainHandler.post {
            try {
                // Always clean up previous instance first to avoid ERROR_RECOGNIZER_BUSY or dual microphone instances
                cleanupRecognizer()

                // ALWAYS use Application Context to prevent context-wrapping ClassCastException or binder crashes in Jetpack Compose
                val appContext = context.applicationContext ?: context
                val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
                if (recognizer == null) {
                    mainHandler.post { onError("Speech engine initialization failed.") }
                    return@post
                }

                speechRecognizer = recognizer
                recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        if (!isActiveSession) return
                        lastActiveTime = System.currentTimeMillis()
                        mainHandler.post { onReadyForSpeech() }
                    }

                    override fun onBeginningOfSpeech() {
                        if (!isActiveSession) return
                        lastActiveTime = System.currentTimeMillis()
                        mainHandler.post { onBeginningOfSpeech() }
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        if (!isActiveSession) return
                        // If user is actively vocalizing, reset the silence timer
                        if (rmsdB > 2.0f) {
                            lastActiveTime = System.currentTimeMillis()
                        }
                        mainHandler.post { onRmsChanged(rmsdB) }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        // Native engine finished this segment.
                        if (!isActiveSession) return
                        
                        val elapsed = System.currentTimeMillis() - lastActiveTime
                        if (elapsed < silenceTimeoutMs) {
                            // Short natural pause or segment ended. Auto-resume quietly!
                            startRecognizerInternal()
                        } else {
                            stopSession()
                        }
                    }

                    override fun onError(error: Int) {
                        if (!isActiveSession) return

                        // Some errors are harmless timeouts or normal segment finishes in continuous mode.
                        // We recover from those by restarting quietly, as long as absolute silence timeout is not exceeded.
                        val isRecoverable = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                            SpeechRecognizer.ERROR_CLIENT -> true
                            else -> false
                        }

                        val elapsed = System.currentTimeMillis() - lastActiveTime
                        if (isRecoverable && elapsed < silenceTimeoutMs) {
                            // Quietly restart
                            mainHandler.postDelayed({
                                startRecognizerInternal()
                            }, 100)
                        } else {
                            // Fatal or actual silence exceeded
                            mainHandler.post {
                                if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                                    onError("Permission denied.")
                                    stopSession(callOnResults = false)
                                } else if (error == SpeechRecognizer.ERROR_NETWORK || error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
                                    onError("Network unavailable.")
                                    stopSession(callOnResults = true)
                                } else {
                                    // Default fallback to gracefully wrap up what we have heard so far instead of discarding
                                    stopSession(callOnResults = true)
                                }
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        if (!isActiveSession) return
                        lastActiveTime = System.currentTimeMillis()

                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val recognized = if (!matches.isNullOrEmpty()) matches[0] else ""

                        mainHandler.post {
                            if (recognized.isNotEmpty()) {
                                finalizedText = buildSentences(finalizedText, recognized)
                            }
                            
                            // Push partial updates live
                            onPartialResults(finalizedText)

                            // Immediately restart to continue capturing speech seamless like WhatsApp dictation
                            startRecognizerInternal()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        if (!isActiveSession) return
                        lastActiveTime = System.currentTimeMillis()

                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val partial = matches[0]
                            mainHandler.post {
                                currentPartialText = partial
                                val combined = buildSentences(finalizedText, partial)
                                onPartialResults(combined)
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

                    // Better linguistic configurations: Urdu + English support
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ur-PK", "ur"))

                    // Google-specific speech recognition duration indicators
                    putExtra("android.speech.extra.DICTATION_MODE", true)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 30000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                }

                speechRecognizer?.startListening(intent)
            } catch (t: Throwable) {
                // If it fails, retry or stop with fallback
                stopSession(callOnResults = true)
            }
        }
    }

    private fun buildSentences(base: String, addition: String): String {
        if (base.isEmpty()) return addition
        if (addition.isEmpty()) return base
        // Combine intelligently verifying spaces and sentence punctuation
        val cleanBase = base.trim()
        val cleanAddition = addition.trim()
        val separator = if (cleanBase.endsWith(".") || cleanBase.endsWith("?") || cleanBase.endsWith("!")) " " else " "
        return "$cleanBase$separator$cleanAddition"
    }

    fun stopListening() {
        stopSession()
    }

    private fun stopSession(callOnResults: Boolean = true) {
        if (!isActiveSession) return
        isActiveSession = false
        mainHandler.removeCallbacks(silenceCheckRunnable)

        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (t: Throwable) {}

            cleanupRecognizer()

            val totalText = buildSentences(finalizedText, currentPartialText).trim()
            
            // Deliver results
            if (callOnResults) {
                onResults(totalText)
            }
            onEndOfSpeech()
        }
    }

    private fun cleanupRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (t: Throwable) {
            // ignore
        } finally {
            speechRecognizer = null
        }
    }

    fun destroy() {
        isActiveSession = false
        mainHandler.removeCallbacks(silenceCheckRunnable)
        cleanupRecognizer()
    }
}

@Composable
fun MicrophoneButton(
    isListening: Boolean,
    rmsDb: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    
    // Smooth infinite breathing scale for the active core button
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_scale"
    )

    // Bouncy spring transition scale when entering/exiting active listening state
    val springScale by animateFloatAsState(
        targetValue = if (isListening) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "spring_scale"
    )

    val finalCoreScale = springScale * (if (isListening) breatheScale else 1.0f)

    // Multi-layered ripple wave 1 (stamped on EaseOutQuart for smooth premium dissipation)
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 3.2f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseOutQuart),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_scale_1"
    )
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.65f else 0.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseOutQuart),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_alpha_1"
    )

    // Multi-layered ripple wave 2 (staggered delay)
    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 3.2f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 500, easing = EaseOutQuart),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_scale_2"
    )
    val waveAlpha2 by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.65f else 0.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 500, easing = EaseOutQuart),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_alpha_2"
    )

    // Multi-layered ripple wave 3 (staggered delay)
    val waveScale3 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 3.2f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 1000, easing = EaseOutQuart),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_scale_3"
    )
    val waveAlpha3 by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.65f else 0.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 1000, easing = EaseOutQuart),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_alpha_3"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(64.dp)
            .testTag("voice_input_mic_button")
    ) {
        if (isListening) {
            // Apply high-sensitivity amplification for low-volume background noise and small voices
            val activeRmsDb = rmsDb.coerceAtLeast(0f)
            val sensitiveAmplifiedRms = if (activeRmsDb > 0.1f) {
                (activeRmsDb * 3.0f) + 1.5f // 3x amplifier with safe low-amplitude floor offset
            } else {
                activeRmsDb * 3.0f
            }
            val voiceDbFactor = (sensitiveAmplifiedRms / 5.0f).coerceAtMost(1.5f)

            // Radiating primary colored ripple layers
            listOf(
                Pair(waveScale1, waveAlpha1),
                Pair(waveScale2, waveAlpha2),
                Pair(waveScale3, waveAlpha3)
            ).forEach { (scaleVal, alphaVal) ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            // Boost scale dynamically with user's speaking voice input volume
                            val dynamicScale = scaleVal + voiceDbFactor * 2.5f
                            scaleX = dynamicScale
                            scaleY = dynamicScale
                            alpha = alphaVal
                        }
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }

        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp) // Meets standard 48.dp x 48.dp accessible touch target size
                .graphicsLayer {
                    scaleX = finalCoreScale
                    scaleY = finalCoreScale
                }
                .background(
                    if (isListening) {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        )
                    },
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (isListening) {
                        Color.White.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    },
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = if (isListening) "Listening active. Tap to cancel" else "Tap to start speaking",
                tint = if (isListening) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun VoiceAmplitudeMeter(
    rmsDb: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 18
) {
    val infiniteTransition = rememberInfiniteTransition(label = "amplitude_wave")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_shift"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // High-sensitivity scale matching
        val rootAmp = (rmsDb.coerceAtLeast(0f) * 3f).coerceIn(0f, 44f)

        for (i in 0 until barCount) {
            val fraction = i.toFloat() / barCount
            val sinFactor = kotlin.math.sin(fraction * 2 * Math.PI + phaseShift).toFloat()
            // Amplitude is driven dynamically by voice DB levels + resting micro-movement
            val calculatedHeight = (rootAmp * (0.25f + 0.75f * kotlin.math.abs(sinFactor)) + 3f).coerceIn(3f, 44f)

            val animatedHeight by animateDpAsState(
                targetValue = calculatedHeight.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "bar_anim_height"
            )

            val barColor = MaterialTheme.colorScheme.primary.copy(
                alpha = (0.35f + 0.65f * (calculatedHeight / 44f)).coerceIn(0.35f, 1.0f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(animatedHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}
