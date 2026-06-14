package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.ThemeSupport
import com.example.ui.theme.ColorAnxiety
import com.example.ui.theme.ColorHappy
import com.example.ui.theme.ColorStress
import com.example.viewmodel.EmotionUiState
import com.example.viewmodel.EmotionViewModel
import java.util.*

@Composable
fun WellnessScreen(
    viewModel: EmotionViewModel,
    innerPadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Breathing Animation State
    var isBreathingActive by remember { mutableStateOf(false) }

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
                text = "WELLNESS PORTAL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Clinical Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // --- Active Emotion Context Card ---
        when (val state = uiState) {
            is EmotionUiState.Success -> {
                val details = ThemeSupport.getEmotionDetails(state.result.dominantEmotion)
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = details.color.copy(alpha = 0.25f),
                    backgroundColor = details.color.copy(alpha = 0.05f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(details.emoji, fontSize = 32.sp)
                        Column {
                            Text(
                                text = "Active Recommendation Guide",
                                style = MaterialTheme.typography.labelSmall,
                                color = details.color,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Targeted exercises for your current ${details.displayName} state:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Based on your latest text, our NLP engines suggest performing the Aura Breathing exercise below. Retaining emotional balance stabilizes autonomic heart rate variations and reduces cortisol counts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            else -> {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tips",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "Linguistic feedback idle",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Complete an emotion classification on the main tab to unlock tailored wellness instructions.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // --- Aura Breathing Sphere (BOX interactive exercise) ---
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp)
        ) {
            Text(
                text = "AURA BREATHING SPHERE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Interactive vagus nerve balance regulator",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pulse Loop animations
            val infiniteTransition = rememberInfiniteTransition(label = "Aura expansion")
            
            // Cycle timeline states: Inhale 4s, Hold 4s, Exhale 4s (total 12s)
            val breathingCycleTime by infiniteTransition.animateValue(
                initialValue = 0,
                targetValue = 12000,
                typeConverter = Int.Companion.VectorConverter,
                animationSpec = infiniteRepeatable(
                    animation = tween(12000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "Cycle timer"
            )

            // Ascending floating particles
            val particle1Y by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = -0.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "P1"
            )
            val particle2Y by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = -0.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4100, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "P2"
            )
            val particle3Y by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = -0.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4900, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "P3"
            )
            val particleAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "PAlpha"
            )
            
            // Calculate scale and caption text dynamically
            val (currentScale, pulseLabel) = if (!isBreathingActive) {
                Pair(1.1f, "Resting State")
            } else {
                // Mapping cycle positions to breathing scale sizes
                val time = breathingCycleTime
                when {
                    time < 4000 -> {
                        // 0s to 4s: Inhaling (expanding from 1.1 to 2.2)
                        val fraction = time / 4000f
                        val scaled = 1.1f + (fraction * 1.1f)
                        Pair(scaled, "Breathe In...")
                    }
                    time < 8000 -> {
                        // 4s to 8s: Holding (stable at 2.2)
                        Pair(2.2f, "Hold Breath...")
                    }
                    else -> {
                        // 8s to 12s: Exhaling (contracting from 2.2 to 1.1)
                        val fraction = (time - 8000) / 4000f
                        val scaled = 2.2f - (fraction * 1.1f)
                        Pair(scaled, "Exhale Slowly...")
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBreathingActive) {
                        // Floating meditation particles Canvas overlay
                        val particlePrimary = MaterialTheme.colorScheme.primary
                        val particleSecondary = MaterialTheme.colorScheme.secondary
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Left side ascending glow
                            drawCircle(
                                color = particlePrimary.copy(alpha = particleAlpha * (1f - particle1Y)),
                                radius = 10f * (1f - particle1Y),
                                center = Offset(size.width * 0.22f, size.height * particle1Y)
                            )
                            // Right side ascending glow
                            drawCircle(
                                color = particleSecondary.copy(alpha = particleAlpha * (1f - particle2Y)),
                                radius = 14f * (1f - particle2Y),
                                center = Offset(size.width * 0.78f, size.height * particle2Y)
                            )
                            // Center staggered glow
                            drawCircle(
                                color = particlePrimary.copy(alpha = particleAlpha * (1f - particle3Y)),
                                radius = 12f * (1f - particle3Y),
                                center = Offset(size.width * 0.48f, size.height * particle3Y)
                            )
                        }
                    }

                    // Pulsing animated gradient Aura circles
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .graphicsLayer {
                                scaleX = currentScale
                                scaleY = currentScale
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isBreathingActive) {
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                            Color.Transparent
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    }
                                )
                            )
                    )

                    // Core breathing orb
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBreathingActive) "🧘" else "💤",
                            fontSize = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = pulseLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isBreathingActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Text(
                    text = if (isBreathingActive) "Follow the expanding circles for 12 seconds per loop" else "Press Start to begin vagus nerve regulation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { isBreathingActive = !isBreathingActive },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBreathingActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isBreathingActive) "Stop Session" else "Start Deep Breathing",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Common Wellness Actions Lists ---
        Text(
            text = "COGNITIVE METHODOLOGIES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )

        // Activity 1: Cognitive reframing
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.background(ColorHappy.copy(alpha = 0.1f), CircleShape).padding(10.dp)
                ) {
                    Text("💡", fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = "Cognitive Reframing Journal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rewrite stressful trigger thoughts. Describe the situation constructively using objective linguistic facts to reprogram automated amygdala responses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Activity 2: Gratitude log
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.background(ColorAnxiety.copy(alpha = 0.1f), CircleShape).padding(10.dp)
                ) {
                    Text("✍️", fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = "Daily Gratitude Affirmations",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Specify 3 concrete things you appreciate of your current day. Journaling appreciations reinforces thalamic filters, shielding focus from passive anxieties.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
