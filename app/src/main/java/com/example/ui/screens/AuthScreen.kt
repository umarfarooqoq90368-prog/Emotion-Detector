package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.viewmodel.EmotionViewModel

enum class AuthScreenType {
    LOGIN, REGISTER, FORGOT_PASSWORD
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    viewModel: EmotionViewModel,
    onAuthSuccess: () -> Unit
) {
    var screenType by remember { mutableStateOf(AuthScreenType.LOGIN) }
    
    // Inputs
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    val authError by viewModel.authError.collectAsState()
    var successMsg by remember { mutableStateOf<String?>(null) }
    var localErrorMsg by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // --- Vector Header Banner ---
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "SaaS Security Core",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "EMOTIONAI PRO",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 2.sp
        )
        
        Text(
            text = "Enterprise Cognitive SaaS Platform",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                text = when (screenType) {
                    AuthScreenType.LOGIN -> "Account Access Gate"
                    AuthScreenType.REGISTER -> "Create SaaS Profile"
                    AuthScreenType.FORGOT_PASSWORD -> "Recover Security Key"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // --- Error / Notifications space ---
            if (authError != null) {
                Text(
                    text = authError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            if (localErrorMsg != null) {
                Text(
                    text = localErrorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            if (successMsg != null) {
                Text(
                    text = successMsg!!,
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            // --- Fields ---
            when (screenType) {
                AuthScreenType.LOGIN -> {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; localErrorMsg = null },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User") },
                        modifier = Modifier.fillMaxWidth().testTag("username_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; localErrorMsg = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("password_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Forgot Password?",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { screenType = AuthScreenType.FORGOT_PASSWORD; localErrorMsg = null; successMsg = null }
                    )
                }
                AuthScreenType.REGISTER -> {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; localErrorMsg = null },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Face, contentDescription = "Face") },
                        modifier = Modifier.fillMaxWidth().testTag("fullname_register_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; localErrorMsg = null },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User") },
                        modifier = Modifier.fillMaxWidth().testTag("username_register_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; localErrorMsg = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth().testTag("email_register_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; localErrorMsg = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("password_register_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; localErrorMsg = null },
                        label = { Text("Verify Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_password_register_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                AuthScreenType.FORGOT_PASSWORD -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; localErrorMsg = null },
                        label = { Text("Registered Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth().testTag("forgot_email_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // --- Submit CTA Button ---
            Button(
                onClick = {
                    when (screenType) {
                        AuthScreenType.LOGIN -> {
                            if (username.isEmpty() || password.isEmpty()) {
                                localErrorMsg = "Please populate all fields."
                            } else {
                                viewModel.login(username, password) {
                                    onAuthSuccess()
                                }
                            }
                        }
                        AuthScreenType.REGISTER -> {
                            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                                localErrorMsg = "Please populate all registration details."
                            } else if (password != confirmPassword) {
                                localErrorMsg = "Passwords do not match."
                            } else {
                                viewModel.register(username, email, password, fullName) {
                                    onAuthSuccess()
                                }
                            }
                        }
                        AuthScreenType.FORGOT_PASSWORD -> {
                            if (email.isEmpty()) {
                                localErrorMsg = "Email is required to dispatch instructions."
                            } else {
                                viewModel.forgotPassword(email) {
                                    successMsg = it
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = when (screenType) {
                        AuthScreenType.LOGIN -> "Authenticate Now"
                        AuthScreenType.REGISTER -> "Register SaaS Account"
                        AuthScreenType.FORGOT_PASSWORD -> "Send Access Link"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // --- Switch Screen Links ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (screenType) {
                        AuthScreenType.LOGIN -> "Don't have an account? Sign Up"
                        AuthScreenType.REGISTER -> "Already have an account? Sign In"
                        AuthScreenType.FORGOT_PASSWORD -> "Back to Account Log In"
                    },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable {
                        screenType = when (screenType) {
                            AuthScreenType.LOGIN -> AuthScreenType.REGISTER
                            AuthScreenType.REGISTER -> AuthScreenType.LOGIN
                            AuthScreenType.FORGOT_PASSWORD -> AuthScreenType.LOGIN
                        }
                        localErrorMsg = null
                        successMsg = null
                    }
                )
            }
        }
    }
}
