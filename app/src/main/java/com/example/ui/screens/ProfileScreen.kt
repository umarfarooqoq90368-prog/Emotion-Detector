package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.viewmodel.EmotionViewModel

@Composable
fun ProfileScreen(
    viewModel: EmotionViewModel,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    
    // States
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val isDailyEnabled by viewModel.isDailyReminderEnabled.collectAsState()
    val isMoodEnabled by viewModel.isMoodReminderEnabled.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    var editFullName by remember { mutableStateOf(fullName) }
    var editEmail by remember { mutableStateOf(email) }
    var showSavedMessage by remember { mutableStateOf(false) }

    // Synchronize inputs when edit mode opened
    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            editFullName = fullName
            editEmail = email
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
                text = "USER PROFILE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "SaaS Settings Control",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // --- Profile Card ---
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vector representation or static initial avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isEditMode) {
                    Text(
                        text = if (fullName.isEmpty()) "Enterprise User" else fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Display Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_fullName_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Contact") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_email_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!isEditMode) {
                        Button(
                            onClick = { isEditMode = true; showSavedMessage = false },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile Details")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { isEditMode = false },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.updateProfile(editFullName, editEmail, isDailyEnabled, isMoodEnabled) {
                                    isEditMode = false
                                    showSavedMessage = true
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Text("Save Changes")
                        }
                    }
                }

                if (authError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = authError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (showSavedMessage) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "SaaS profile securely synchronized!",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Notification Controls Section ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Preferences & Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text("Daily Wellness Prompts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Notify daily to track mental and emotional tracks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = isDailyEnabled,
                        onCheckedChange = { viewModel.updateProfile(fullName, email, it, isMoodEnabled) {} },
                        modifier = Modifier.testTag("daily_reminder_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text("Mood Tracking Reminders", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Prompts to trigger check-in intervals throughout workdays", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = isMoodEnabled,
                        onCheckedChange = { viewModel.updateProfile(fullName, email, isDailyEnabled, it) {} },
                        modifier = Modifier.testTag("mood_reminder_switch")
                    )
                }

                val isAutoAnalyzeEnabled by viewModel.isAutoAnalyzeEnabled.collectAsState()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text("Auto-Analyze Voice Input", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Initiate analytical diagnostics immediately when speech completes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = isAutoAnalyzeEnabled,
                        onCheckedChange = { viewModel.setAutoAnalyzeEnabled(it) },
                        modifier = Modifier.testTag("profile_auto_analyze_switch")
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Action buttons to send notification immediately
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Diagnostic Notification Testers",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testInstantNotification("DAILY") },
                            modifier = Modifier.weight(1f).testTag("test_daily_notif_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Trigger Daily", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.testInstantNotification("MOOD") },
                            modifier = Modifier.weight(1f).testTag("test_mood_notif_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Trigger Mood", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Document Export Backups ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "System Backup & Exports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Download historical tracking vectors compiled into secure encrypted documents.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportPdfReport(context) },
                        modifier = Modifier.weight(1f).testTag("export_pdf_profile"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "PDF")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.exportCsvReport(context) },
                        modifier = Modifier.weight(1f).testTag("export_csv_profile"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "CSV")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Logout Button ---
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().testTag("logout_button"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Log Out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Disconnect Enterprise SaaS Session", fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
