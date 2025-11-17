/*
 * ============================================================================
 * LOGIN SCREEN - User Authentication Interface
 * ============================================================================
 *
 * Compose screen for user login and authentication.
 * Handles credential validation, session creation, and navigation routing.
 *
 * UPDATED: All hardcoded strings replaced with string resources
 *
 * ============================================================================
 */

package com.example.routeify.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.R
import com.example.routeify.ui.viewmodel.AuthViewModel
import com.example.routeify.ui.theme.RouteifyBlue500
import com.example.routeify.ui.theme.RouteifyGreen500
import com.example.routeify.utils.BiometricAuthManager

/**
 * Helper function to find FragmentActivity from Context
 * Handles ContextWrapper cases in Compose
 */
private fun Context.findActivity(): FragmentActivity? {
    var context = this
    // Check if the context itself is already a FragmentActivity
    if (context is FragmentActivity) return context
    
    // Unwrap ContextWrappers to find the activity
    while (context is ContextWrapper) {
        context = context.baseContext
        if (context is FragmentActivity) return context
    }
    return null
}

// Login screen composable
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val activity = remember(view) { 
        val viewContext = view.context
        when {
            viewContext is FragmentActivity -> viewContext
            else -> viewContext.findActivity()
        }
    }

    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showBiometricError by remember { mutableStateOf(false) }
    var biometricErrorMessage by remember { mutableStateOf("") }
    var biometricPromptShown by remember { mutableStateOf(false) }
    
    // Initialize biometric manager
    val biometricManager = remember(activity) {
        activity?.let { BiometricAuthManager(it) }
    }
    
    // Show biometric prompt on screen load if enabled and available
    LaunchedEffect(authState.biometricEnabled) {
        // Only show if biometric is enabled, available, and we haven't shown it yet
        if (authState.biometricEnabled && 
            biometricManager?.canAuthenticate() == true && 
            !biometricPromptShown &&
            !authState.isAuthenticated) {
            biometricPromptShown = true
            biometricManager.authenticate(
                title = "Sign in to Routeify",
                subtitle = "Use biometric to continue",
                description = "Authenticate to access your account",
                onSuccess = {
                    authViewModel.loginWithBiometric()
                },
                onError = { _, message ->
                    showBiometricError = true
                    biometricErrorMessage = message
                    biometricPromptShown = false // Allow retry
                }
            )
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) onLoginSuccess()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(RouteifyBlue500, RouteifyGreen500)))
                .padding(vertical = 48.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // App icon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                // App title
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.login_welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Form content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Email/Username field
            Spacer(modifier = Modifier.height(8.dp))

            // Email/Username field
            OutlinedTextField(
                value = emailOrUsername,
                onValueChange = { emailOrUsername = it },
                label = { Text(stringResource(R.string.login_email_username)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RouteifyBlue500,
                    focusedLabelColor = RouteifyBlue500
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.login_password)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RouteifyBlue500,
                    focusedLabelColor = RouteifyBlue500
                )
            )

            // Show error message if login fails
            if (authState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = authState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In button
            Button(
                onClick = { authViewModel.login(emailOrUsername, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RouteifyBlue500
                )
            ) {
                Text(stringResource(R.string.button_sign_in), style = MaterialTheme.typography.titleMedium)
            }
            
            // Biometric login button (if available)
            if (biometricManager?.canAuthenticate() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        biometricManager.authenticate(
                            title = "Sign in to Routeify",
                            subtitle = "Use biometric to continue",
                            description = "Authenticate to access your account",
                            onSuccess = {
                                authViewModel.loginWithBiometric()
                            },
                            onError = { _, message ->
                                showBiometricError = true
                                biometricErrorMessage = message
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RouteifyGreen500
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign in with Biometric", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            // Show biometric error if any
            if (showBiometricError) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = biometricErrorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Create Account button
            Spacer(modifier = Modifier.height(12.dp))

            // Outlined button for account creation
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RouteifyBlue500
                )
            ) {
                // Create Account text
                Text(stringResource(R.string.button_create_account), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with "OR" text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Divider
                Divider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.login_or),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign-In button
            OutlinedButton(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                // Google icon and text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google icon
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.login_google), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}


// --------------------------------------------------End of File----------------------------------------------------------------