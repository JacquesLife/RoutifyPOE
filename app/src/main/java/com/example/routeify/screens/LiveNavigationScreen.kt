package com.example.routeify.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.routeify.data.Route
import com.example.routeify.data.RouteStep
import com.example.routeify.data.StepType
import com.example.routeify.data.MockRouteData
import com.example.routeify.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LiveNavigationScreen(
    route: Route? = MockRouteData.sampleRoute,
    onNavigateToHome: () -> Unit = {}
) {
    var currentStepIndex by remember { mutableStateOf(0) }
    var showAlert by remember { mutableStateOf(true) }
    
    // Simulate navigation progress - move to next step every 10 seconds
    LaunchedEffect(route) {
        while (route != null && currentStepIndex < route.steps.size - 1) {
            delay(10000) // 10 seconds
            currentStepIndex++
        }
    }
    
    if (route == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No route available for navigation",
                color = Color.White
            )
        }
        return
    }
    
    val currentStep = route.steps[currentStepIndex]
    val nextStep = if (currentStepIndex < route.steps.size - 1) {
        route.steps[currentStepIndex + 1]
    } else null
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827)) // bg-gray-900
    ) {
        // Full-screen Map Background
        MapBackground(
            modifier = Modifier.fillMaxSize()
        )
        
        // Exit Button
        IconButton(
            onClick = onNavigateToHome,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .zIndex(10f)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Exit Navigation",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Current Step Overlay
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .zIndex(10f),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDBEAFE)), // bg-blue-100
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getStepIcon(currentStep.type),
                        contentDescription = currentStep.type.name,
                        tint = Color(0xFF2563EB), // text-blue-600
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentStep.description,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827) // text-gray-900
                    )
                    Text(
                        text = "${currentStep.duration} remaining",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280) // text-gray-600
                    )
                }
            }
        }
        
        // Next Step Preview
        nextStep?.let { step ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 128.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .zIndex(10f),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6)), // bg-gray-100
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getStepIcon(step.type),
                            contentDescription = step.type.name,
                            tint = Color(0xFF6B7280), // text-gray-600
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Next: ${step.description}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111827) // text-gray-900
                        )
                        Text(
                            text = step.duration,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280) // text-gray-600
                        )
                    }
                }
            }
        }
        
        // Alert for delays/disruptions
        if (showAlert) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 192.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .zIndex(10f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEFCE8) // bg-yellow-50
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFD97706), // text-yellow-600
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Minor delay on Southern Line - 3 minutes behind schedule",
                        fontSize = 14.sp,
                        color = Color(0xFF92400E), // text-yellow-800
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = { showAlert = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Alert",
                            tint = Color(0xFFD97706), // text-yellow-600
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
        
        // Progress Indicator
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .zIndex(10f),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Step ${currentStepIndex + 1} of ${route.steps.size}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280) // text-gray-600
                    )
                    Text(
                        text = "ETA: ${route.arrivalTime}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280) // text-gray-600
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE5E7EB)) // bg-gray-200
                ) {
                    val progress = (currentStepIndex + 1).toFloat() / route.steps.size.toFloat()
                    
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2563EB)) // bg-blue-600
                    )
                }
            }
        }
    }
}

@Composable
fun MapBackground(
    modifier: Modifier = Modifier
) {
    // Animation for user location pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF14532D), // from-green-900
                        Color(0xFF1E3A8A), // via-blue-900
                        Color(0xFF166534)  // to-green-800
                    ),
                    radius = 1000f
                )
            )
    ) {
        // Mock route path
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val path = Path().apply {
                moveTo(size.width * 0.125f, size.height * 0.5f)
                quadraticBezierTo(
                    size.width * 0.375f, size.height * 0.25f,
                    size.width * 0.625f, size.height * 0.375f
                )
                quadraticBezierTo(
                    size.width * 0.875f, size.height * 0.5f,
                    size.width * 1f, size.height * 0.45f
                )
            }
            
            drawPath(
                path = path,
                color = Color(0xFF60A5FA).copy(alpha = 0.6f), // blue-400 with opacity
                style = Stroke(
                    width = 8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f))
                )
            )
        }
        
        // User location (center of screen)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6)) // bg-blue-500
                .zIndex(10f)
        )
        
        // Pulse animation for user location
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(Color(0xFF60A5FA).copy(alpha = 0.3f)) // bg-blue-400 with opacity
                .zIndex(9f)
        )
    }
}

fun getStepIcon(stepType: StepType): ImageVector {
    return when (stepType) {
        StepType.WALK -> Icons.Default.DirectionsWalk
        StepType.TRAIN -> Icons.Default.Train
        StepType.BUS -> Icons.Default.DirectionsBus
        StepType.TAXI -> Icons.Default.LocalTaxi
    }
}

@Preview(showBackground = true)
@Composable
fun LiveNavigationScreenPreview() {
    RouteifyTheme {
        LiveNavigationScreen()
    }
}
