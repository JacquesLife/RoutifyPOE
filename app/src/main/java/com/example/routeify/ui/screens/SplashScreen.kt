/*
 * ============================================================================
 * SPLASH SCREEN - App Startup & Loading Interface
 * ============================================================================
 * 
 * Initial Compose screen with app branding and loading animation.
 * Handles app initialization and smooth transition to main screens.
 * 
 * ============================================================================
 */

package com.example.routeify.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routeify.R
import com.example.routeify.ui.theme.RouteifyBlue500
import com.example.routeify.ui.theme.RouteifyGreen500
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Simple fade-in for subtle polish
    val started = remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (started.value) 1f else 0f, label = "splash-alpha")

    LaunchedEffect(Unit) {
        started.value = true
        // Keep the splash visible briefly, then continue
        delay(1200)
        onFinished()
    }

    // Gradient background
    val gradient = Brush.horizontalGradient(
        colors = listOf(RouteifyBlue500, RouteifyGreen500)
    )

    // Fullscreen box with centered content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        // App logo and name
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(alpha)) {
            Surface(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(12.dp, CircleShape),
                shape = CircleShape,
                color = Color.White
            ) {
                // Logo
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Routeify logo",
                        tint = RouteifyBlue500,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App name and tagline
            Text(
                text = "Routeify",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Smarter Public Transport",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------