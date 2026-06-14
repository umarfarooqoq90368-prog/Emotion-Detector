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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.ThemeSupport
import com.example.ui.theme.ColorAnxiety
import com.example.ui.theme.ColorNeutral
import com.example.ui.theme.ColorStress
import com.example.viewmodel.AnalyticsPoint
import com.example.viewmodel.AnalyticsTimeframe
import com.example.viewmodel.EmotionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: EmotionViewModel,
    innerPadding: PaddingValues
) {
    val analyticsTimeframe by viewModel.analyticsTimeframe.collectAsState()
    val summary by viewModel.analyticsSummary.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Alert dialog states for premium export simulations
    var showExportDialog by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ANALYTICS ENGINE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mood Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Quick premium PDF action button
            IconButton(
                onClick = {
                    exportType = "PDF Report"
                    showExportDialog = true
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export report",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // --- Timeframe selector tabs ---
        TabRow(
            selectedTabIndex = analyticsTimeframe.ordinal,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.1.dp) // customized out default indicator
                )
            }
        ) {
            AnalyticsTimeframe.values().forEach { timeframe ->
                val selected = analyticsTimeframe == timeframe
                Tab(
                    selected = selected,
                    onClick = { viewModel.setTimeframe(timeframe) },
                    text = {
                        Text(
                            text = when (timeframe) {
                                AnalyticsTimeframe.DAILY -> "Daily Trace"
                                AnalyticsTimeframe.WEEKLY -> "Weekly Log"
                                AnalyticsTimeframe.MONTHLY -> "Monthly Trend"
                            },
                            fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                )
            }
        }

        if (summary.totalAnalyses == 0) {
            // Empty state
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                contentPadding = PaddingValues(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "No data",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Historical analytics empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please write and analyze statements in the 'Analyze' panel first. Predictions completed locally or remotely will populate dashboard curves instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // --- Core metrics grid ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Key total analyses metric
                GlassCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text(
                        text = "Total Logs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CountUpText(
                        targetValue = summary.totalAnalyses,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Recorded evaluations",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 9.sp
                    )
                }

                // Balance Index Metric
                val dominantDetails = ThemeSupport.getEmotionDetails(summary.primaryOverallEmotion)
                GlassCard(
                    modifier = Modifier.weight(1.2f),
                    borderColor = dominantDetails.color.copy(alpha = 0.25f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text(
                        text = "Overall Feeling",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(dominantDetails.emoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dominantDetails.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Most frequent state",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 9.sp
                    )
                }
            }

            // --- Secondary index gauges deck ---
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "STRESS & MENTAL WELLNESS METRICS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Score 1: Wellness
                    val animatedWellnessIndex by animateFloatAsState(
                        targetValue = summary.averageWellnessScore / 100f,
                        animationSpec = tween(1300, easing = EaseOutCubic),
                        label = "wellness_donut_index"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                            CircularProgressIndicator(
                                progress = { animatedWellnessIndex },
                                color = ColorAnxiety,
                                strokeWidth = 6.dp,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxSize()
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${summary.averageWellnessScore.toInt()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Text("pts", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Wellness Index",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "General balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    // Score 2: Stress
                    val animatedStressIndex by animateFloatAsState(
                        targetValue = summary.averageStressLevel,
                        animationSpec = tween(1300, easing = EaseOutCubic),
                        label = "stress_donut_index"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                            CircularProgressIndicator(
                                progress = { animatedStressIndex },
                                color = ColorStress,
                                strokeWidth = 6.dp,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxSize()
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(summary.averageStressLevel * 100).toInt()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Text("%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Stress Capacity",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Active workload",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // --- Donut Chart Card (Emotion Distribution) ---
            if (summary.emotionDistribution.isNotEmpty()) {
                var chartTrigger by remember { mutableStateOf(false) }
                LaunchedEffect(summary) {
                    chartTrigger = true
                }
                val chartAnimationProgress by animateFloatAsState(
                    targetValue = if (chartTrigger) 1f else 0f,
                    animationSpec = tween(durationMillis = 1400, easing = EaseOutCubic),
                    label = "donut_chart_entry"
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "EMOTION DISTRIBUTION SPECTRUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drawing the pie/donut segments
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(110.dp)) {
                                var currentStartAngle = -90f
                                summary.emotionDistribution.forEach { entry ->
                                    val emotion = entry.key
                                    val percentage = entry.value
                                    val sweep = (percentage / 100f) * 360f
                                    val emotDetails = ThemeSupport.getEmotionDetails(emotion)
                                    drawArc(
                                        color = emotDetails.color,
                                        startAngle = currentStartAngle,
                                        sweepAngle = sweep * chartAnimationProgress,
                                        useCenter = false,
                                        style = Stroke(width = 20f, cap = StrokeCap.Round),
                                        size = Size(size.width, size.height)
                                    )
                                    currentStartAngle += sweep * chartAnimationProgress
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${summary.emotionDistribution.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Vibe Types",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // Legenda showing percentages
                        Column(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            summary.emotionDistribution.toList().sortedByDescending { it.second }.forEach { pair ->
                                val emotion = pair.first
                                val pct = pair.second
                                val details = ThemeSupport.getEmotionDetails(emotion)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(details.color, CircleShape)
                                    )
                                    Text(
                                        text = "${details.displayName}: ${pct.toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Historical Trend Line Chart ---
            if (summary.historicalPoints.isNotEmpty()) {
                var graphTrigger by remember { mutableStateOf(false) }
                LaunchedEffect(summary) {
                    graphTrigger = true
                }
                val graphAnimationProgress by animateFloatAsState(
                    targetValue = if (graphTrigger) 1f else 0f,
                    animationSpec = tween(1600, easing = EaseOutCubic),
                    label = "chrono_graph_entry"
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "STRESS & WELLNESS CHRONO-TRACK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Trace line of wellness (cyan) vs stress intensity (orange) over chronological inputs:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pts = summary.historicalPoints
                            if (pts.size < 2) {
                                // Draw placeholder message or single dot
                                drawCircle(
                                    color = ColorAnxiety,
                                    radius = 8f,
                                    center = Offset(size.width / 2, size.height / 2)
                                )
                                return@Canvas
                            }

                            // Calculate coordinate mappings
                            val widthInterval = size.width / (pts.size - 1)
                            val heightMax = size.height - 20f
                            val heightMin = 20f

                            val wellnessPath = Path()
                            val stressPath = Path()

                            pts.forEachIndexed { index, point ->
                                val xCoord = index * widthInterval
                                
                                // Map scores (stress is 0-1, wellness is 0-100)
                                val wYDelta = ((point.wellnessScore / 100f) * (heightMax - heightMin)) + heightMin
                                val sYDelta = (point.stressScore * (heightMax - heightMin)) + heightMin

                                val wYCoord = size.height - (wYDelta * graphAnimationProgress)
                                val sYCoord = size.height - (sYDelta * graphAnimationProgress)

                                if (index == 0) {
                                    wellnessPath.moveTo(xCoord, wYCoord)
                                    stressPath.moveTo(xCoord, sYCoord)
                                } else {
                                    wellnessPath.lineTo(xCoord, wYCoord)
                                    stressPath.lineTo(xCoord, sYCoord)
                                }

                                // Render small dots at index node points
                                drawCircle(
                                    color = ColorAnxiety,
                                    radius = 5f * graphAnimationProgress,
                                    center = Offset(xCoord, wYCoord)
                                )
                                drawCircle(
                                    color = ColorStress,
                                    radius = 5f * graphAnimationProgress,
                                    center = Offset(xCoord, sYCoord)
                                )
                            }

                            // Draw curves
                            drawPath(
                                path = wellnessPath,
                                color = ColorAnxiety,
                                style = Stroke(width = 4f * graphAnimationProgress, cap = StrokeCap.Round)
                            )
                            drawPath(
                                path = stressPath,
                                color = ColorStress,
                                style = Stroke(width = 4f * graphAnimationProgress, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val simpleFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                        val firstTimestamp = summary.historicalPoints.first().timestamp
                        val lastTimestamp = summary.historicalPoints.last().timestamp
                        
                        Text(
                            text = simpleFormat.format(Date(firstTimestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Historical Timeline Trace",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = simpleFormat.format(Date(lastTimestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // --- Premium Export Actions Panel ---
            Text(
                text = "ENTERPRISE PRIVILEGES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Premium",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pro Export Platform",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Extract secure PDF metrics or raw CSV timelines",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                exportType = "CSV Dataset File"
                                showExportDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("CSV", fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = {
                                exportType = "PDF Report File"
                                showExportDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("PDF", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }

    // --- Premium simulation alert dialog ---
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "PDF Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Premium Export Initiated")
                }
            },
            text = {
                Column {
                    Text(
                        text = "We have structured a beautiful, compliant document represent for this $exportType.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Details compiled:\n• Total Entries: ${summary.totalAnalyses}\n• Balance Index: ${summary.primaryOverallEmotion}\n• Average Wellness score: ${summary.averageWellnessScore.toInt()} pts\n• Generated Time: ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Decline", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Download", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
fun CountUpText(
    targetValue: Int,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    suffix: String = ""
) {
    val animateVal by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutQuad),
        label = "count_up"
    )
    Text(
        text = "$animateVal$suffix",
        style = style,
        color = color,
        modifier = modifier
    )
}

