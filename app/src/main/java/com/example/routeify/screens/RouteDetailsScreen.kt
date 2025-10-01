package com.example.routeify.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.routeify.data.Route
import com.example.routeify.data.RouteStep
import com.example.routeify.data.StepType
import com.example.routeify.data.MockRouteData
import com.example.routeify.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailsScreen(
    route: Route? = MockRouteData.sampleRoute,
    onNavigateBack: () -> Unit = {},
    onStartNavigation: () -> Unit = {}
) {
    if (route == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No route selected",
                color = TextSecondary
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // bg-gray-50
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .padding(top = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Text(
                    text = "Route Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
        
        // Map Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp) // h-48
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFDCFCE7), // from-green-100
                            Color(0xFFDBEAFE)  // to-blue-100
                        ),
                        radius = 800f
                    )
                )
        ) {
            // Mock route line
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val path = Path().apply {
                    moveTo(size.width * 0.05f, size.height * 0.33f)
                    quadraticBezierTo(
                        size.width * 0.25f, size.height * 0.17f,
                        size.width * 0.45f, size.height * 0.5f
                    )
                    quadraticBezierTo(
                        size.width * 0.65f, size.height * 0.83f,
                        size.width * 0.85f, size.height * 0.67f
                    )
                }
                
                drawPath(
                    path = path,
                    color = Color(0xFF3B82F6), // blue-600
                    style = Stroke(
                        width = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )
            }
            
            // Center content
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = "Map Preview",
                            tint = RouteifyBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Interactive Map Preview",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151) // text-gray-700
                )
                Text(
                    text = "Route visualization",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
        
        // Route Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "08:45 - ${route.arrivalTime}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "(${route.totalDuration})",
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                }
                
                Text(
                    text = route.price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF16A34A) // text-green-600
                )
            }
        }
        
        // Step-by-step Timeline
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Journey Steps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            itemsIndexed(route.steps) { index, step ->
                RouteStepItem(
                    step = step,
                    isLast = index == route.steps.size - 1
                )
            }
        }
        
        // Start Navigation Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
        ) {
            Button(
                onClick = onStartNavigation,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RouteifyBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Start Navigation",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Navigation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RouteStepItem(
    step: RouteStep,
    isLast: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6)), // bg-gray-100
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getStepIcon(step.type),
                    contentDescription = step.type.name,
                    tint = getStepIconColor(step.type),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Color(0xFFD1D5DB)) // bg-gray-300
                )
            }
        }
        
        // Step details
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = step.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = step.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = step.duration,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }
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

fun getStepIconColor(stepType: StepType): Color {
    return when (stepType) {
        StepType.WALK -> Color(0xFF6B7280) // text-gray-600
        StepType.TRAIN -> Color(0xFF16A34A) // text-green-600
        StepType.BUS -> Color(0xFF2563EB) // text-blue-600
        StepType.TAXI -> Color(0xFFD97706) // text-yellow-600
    }
}

@Preview(showBackground = true)
@Composable
fun RouteDetailsScreenPreview() {
    RouteifyTheme {
        RouteDetailsScreen()
    }
}
