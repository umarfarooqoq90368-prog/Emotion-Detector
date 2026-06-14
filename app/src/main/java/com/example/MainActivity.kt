package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyzeScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.WellnessScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EmotionViewModel

enum class NavigationTab {
    ANALYZE, ANALYTICS, HISTORY, WELLNESS, PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainLayout()
            }
        }
    }
}

@Composable
fun MainLayout() {
    val viewModel: EmotionViewModel = viewModel()
    var isSplashActive by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(NavigationTab.ANALYZE) }
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    if (isSplashActive) {
        com.example.ui.screens.SplashScreen(
            onDismiss = { isSplashActive = false }
        )
    } else if (!isLoggedIn) {
        AuthScreen(viewModel = viewModel) {
            currentTab = NavigationTab.ANALYZE
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    // Tab 1: Analyze
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.ANALYZE,
                        onClick = { currentTab = NavigationTab.ANALYZE },
                        label = { Text("Analyze", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Analyze Spectrum"
                            )
                        }
                    )

                    // Tab 2: Dashboard
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.ANALYTICS,
                        onClick = { currentTab = NavigationTab.ANALYTICS },
                        label = { Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Analytics Dashboard"
                            )
                        }
                    )

                    // Tab 3: History Logs
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.HISTORY,
                        onClick = { currentTab = NavigationTab.HISTORY },
                        label = { Text("History", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Search history logs"
                            )
                        }
                    )

                    // Tab 4: Wellness Hub
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.WELLNESS,
                        onClick = { currentTab = NavigationTab.WELLNESS },
                        label = { Text("Wellness", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Breathing Regulator"
                            )
                        }
                    )

                    // Tab 5: Profile & Settings
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.PROFILE,
                        onClick = { currentTab = NavigationTab.PROFILE },
                        label = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "SaaS Settings Control"
                            )
                        }
                    )
                }
            }
        ) { innerPadding ->
            // Transition or static navigation router mapping
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentTab) {
                    NavigationTab.ANALYZE -> {
                        AnalyzeScreen(viewModel = viewModel, innerPadding = innerPadding)
                    }
                    NavigationTab.ANALYTICS -> {
                        DashboardScreen(viewModel = viewModel, innerPadding = innerPadding)
                    }
                    NavigationTab.HISTORY -> {
                        HistoryScreen(
                            viewModel = viewModel,
                            innerPadding = innerPadding,
                            onNavigateToAnalyze = { currentTab = NavigationTab.ANALYZE }
                        )
                    }
                    NavigationTab.WELLNESS -> {
                        WellnessScreen(viewModel = viewModel, innerPadding = innerPadding)
                    }
                    NavigationTab.PROFILE -> {
                        ProfileScreen(viewModel = viewModel, innerPadding = innerPadding)
                    }
                }
            }
        }
    }
}
