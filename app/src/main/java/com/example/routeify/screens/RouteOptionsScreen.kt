package com.example.routeify.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.routeify.data.Route
import com.example.routeify.data.StepType
import com.example.routeify.data.MockRouteData
import com.example.routeify.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteOptionsScreen(
    destination: String = "Claremont Station",
    onRouteSelect: (Route) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val routes = remember { MockRouteData.mockRoutes }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // bg-gray-50
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                    
                    Column {
                        Text(
                            text = "Route Options",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "To $destination",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            // Route Cards
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(routes) { route ->
                    RouteCard(
                        route = route,
                        onClick = { onRouteSelect(route) }
                    )
                }
            }
        }
        
        // Floating Filter Button
        FloatingActionButton(
            onClick = { /* Handle filter */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp)
                .size(56.dp)
                .zIndex(10f),
            containerColor = RouteifyBlue,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Filter Routes",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun RouteCard(
    route: Route,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Time and Transport Mode Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Departure Time
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = route.departureTime,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Depart",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                
                // Transport Modes
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    route.transportModes.forEachIndexed { index, mode ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(RouteifyBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getTransportIcon(mode),
                                contentDescription = mode.name,
                                tint = RouteifyBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        if (index < route.transportModes.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(1.dp)
                                    .background(Color(0xFFD1D5DB)) // bg-gray-300
                            )
                        }
                    }
                }
                
                // Arrival Time
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = route.arrivalTime,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Arrive",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Duration and Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = route.totalDuration,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = route.price,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16A34A) // text-green-600
                    )
                }
            }
        }
    }
}

fun getTransportIcon(stepType: StepType): ImageVector {
    return when (stepType) {
        StepType.WALK -> Icons.Default.DirectionsWalk
        StepType.TRAIN -> Icons.Default.Train
        StepType.BUS -> Icons.Default.DirectionsBus
        StepType.TAXI -> Icons.Default.LocalTaxi
    }
}

@Preview(showBackground = true)
@Composable
fun RouteOptionsScreenPreview() {
    RouteifyTheme {
        RouteOptionsScreen()
    }
}
