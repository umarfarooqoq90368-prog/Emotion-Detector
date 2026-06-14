package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SecondaryDark
import kotlinx.coroutines.delay
import kotlin.random.Random

// Represents a floating particle in the background
private data class SplashParticle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: Color,
    val maxAlpha: Float
)

@Composable
fun SplashScreen(
    onDismiss: () -> Unit
) {
    var animateStart by remember { mutableStateOf(false) }

    // Start delay to dismiss splash
    LaunchedEffect(Unit) {
        animateStart = true
        delay(2600) // 2.6 seconds total display time
        onDismiss()
    }

    // Spring scaling animation for key logo
    val scaleFactor by animateFloatAsState(
        targetValue = if (animateStart) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Logo bounce reveal"
    )

    // Fade-in animation for title and subtitles
    val fadeAlpha by animateFloatAsState(
        targetValue = if (animateStart) 1f else 0f,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "Text fade reveal"
    )

    // Pulsing outer halo for logo glow
    val infiniteTransition = rememberInfiniteTransition(label = "Halo Pulsing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow size"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow alpha"
    )

    // Particle system state
    val particles = remember {
        mutableStateListOf<SplashParticle>().apply {
            repeat(45) {
                add(
                    SplashParticle(
                        x = Random.nextFloat(),
                        y = Random.nextFloat(),
                        vx = (Random.nextFloat() - 0.5f) * 0.002f,
                        vy = (Random.nextFloat() - 0.5f) * 0.002f,
                        radius = Random.nextFloat() * 6f + 3f,
                        color = if (Random.nextBoolean()) PrimaryDark else SecondaryDark,
                        maxAlpha = Random.nextFloat() * 0.5f + 0.1f
                    )
                )
            }
        }
    }

    // Particle tick updates driven by a system timer ticker
    val tickTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ticker frames"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF13172E),
                        BackgroundDark
                    ),
                    center = Offset.Unspecified,
                    radius = Float.NaN
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // --- Particle canvas drawing layer ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Trigger redraw on each frame tick update
            val dummyVal = tickTime 
            
            particles.forEach { p ->
                // Update position
                p.x = (p.x + p.vx + 1f) % 1f
                p.y = (p.y + p.vy + 1f) % 1f
                
                val pixelX = p.x * size.width
                val pixelY = p.y * size.height
                
                drawCircle(
                    color = p.color.copy(alpha = p.maxAlpha),
                    radius = p.radius,
                    center = Offset(pixelX, pixelY)
                )
            }
        }

        // --- Core Logo reveal with halo animations ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(scaleFactor)
            ) {
                // Outer Pulse Halo Ring
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PrimaryDark.copy(alpha = 0.45f),
                                    SecondaryDark.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Secondary Inner Ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(1.15f)
                        .alpha(0.12f)
                        .background(SecondaryDark, shape = CircleShape)
                )

                // Logo container card
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PrimaryDark,
                                    SecondaryDark
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "EmotionAI Logo",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand title display with glowing text shadow
            Text(
                text = "EMOTIONAI PRO",
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(
                        color = PrimaryDark.copy(alpha = 0.8f),
                        offset = Offset(0f, 0f),
                        blurRadius = 24f
                    )
                ),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 5.sp,
                modifier = Modifier.alpha(fadeAlpha)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Cognitive Enterprise SaaS",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.45f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.alpha(fadeAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "SECURE COGNITIVE DEEP LEARNING",
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryDark.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(fadeAlpha)
            )
        }
    }
}
