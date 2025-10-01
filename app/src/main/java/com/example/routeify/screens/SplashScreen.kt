package com.example.routeify.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routeify.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    // Auto-navigate after 2 seconds
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GradientStart,
                        Color(0xFF3B82F6), // Blue-600 equivalent
                        GradientEnd
                    ),
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Logo Circle with shadow effect
            Card(
                modifier = Modifier.size(96.dp), // 24 * 4 = 96dp (w-24 h-24)
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp) // shadow-2xl
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Routeify Logo",
                        modifier = Modifier.size(48.dp), // w-12 h-12
                        tint = Color(0xFF2563EB) // text-blue-600
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp)) // space-y-8
            
            // Text content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Name
                Text(
                    text = "Routeify",
                    fontSize = 24.sp, // text-2xl
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp)) // mb-2
                
                // Tagline
                Text(
                    text = "Smarter Public Transport",
                    fontSize = 18.sp, // text-lg
                    color = Color.White.copy(alpha = 0.9f) // text-white/90
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    RouteifyTheme {
        SplashScreen(onNavigateToHome = {})
    }
}
