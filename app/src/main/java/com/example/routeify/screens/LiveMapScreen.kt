package com.example.routeify.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.routeify.components.BottomNavigationBar
import com.example.routeify.ui.theme.*

data class MapPin(
    val id: String,
    val type: TransportType,
    val name: String,
    val nextDeparture: String,
    val coordinates: Offset
)

enum class TransportType {
    BUS, TRAIN, TAXI
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMapScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToRouteOptions: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilters by remember { mutableStateOf(setOf(TransportType.BUS, TransportType.TRAIN, TransportType.TAXI)) }
    var selectedPin by remember { mutableStateOf<MapPin?>(null) }
    
    val mapPins = remember {
        listOf(
            MapPin("1", TransportType.TRAIN, "Rondebosch Station", "2 min", Offset(150f, 200f)),
            MapPin("2", TransportType.BUS, "Main Road Bus Stop", "5 min", Offset(200f, 150f)),
            MapPin("3", TransportType.TRAIN, "Claremont Station", "8 min", Offset(280f, 180f)),
            MapPin("4", TransportType.BUS, "University Bus Stop", "12 min", Offset(120f, 250f)),
            MapPin("5", TransportType.TAXI, "Taxi Rank - Wynberg", "Now", Offset(320f, 220f)),
            MapPin("6", TransportType.BUS, "Hospital Bus Stop", "15 min", Offset(250f, 280f))
        )
    }
    
    val filteredPins = mapPins.filter { activeFilters.contains(it.type) }
    
    fun toggleFilter(type: TransportType) {
        activeFilters = if (activeFilters.contains(type)) {
            activeFilters - type
        } else {
            activeFilters + type
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9)) // bg-gray-100
    ) {
        // Map Area
        MapArea(
            pins = filteredPins,
            onPinClick = { selectedPin = it },
            modifier = Modifier.fillMaxSize()
        )
        
        // Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(10f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { 
                    Text(
                        "Search stops or stations",
                        color = TextSecondary
                    ) 
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Handle search */ })
            )
        }
        
        // Filter Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp, start = 16.dp, end = 16.dp)
                .zIndex(10f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterButton(
                type = TransportType.BUS,
                icon = Icons.Default.DirectionsBus,
                label = "Bus",
                isActive = activeFilters.contains(TransportType.BUS),
                onClick = { toggleFilter(TransportType.BUS) }
            )
            FilterButton(
                type = TransportType.TRAIN,
                icon = Icons.Default.Train,
                label = "Train",
                isActive = activeFilters.contains(TransportType.TRAIN),
                onClick = { toggleFilter(TransportType.TRAIN) }
            )
            FilterButton(
                type = TransportType.TAXI,
                icon = Icons.Default.LocalTaxi,
                label = "Taxi",
                isActive = activeFilters.contains(TransportType.TAXI),
                onClick = { toggleFilter(TransportType.TAXI) }
            )
        }
        
        // Pin Details Popup
        selectedPin?.let { pin ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 180.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .zIndex(20f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(getPinColor(pin.type)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getPinIcon(pin.type),
                                contentDescription = pin.type.name,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = pin.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Next departure: ${pin.nextDeparture}",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { selectedPin = null },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }
        
        // Bottom Navigation
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomNavigationBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToRouteOptions = onNavigateToRouteOptions,
                onNavigateToProfile = onNavigateToProfile,
                currentScreen = "map"
            )
        }
    }
}

@Composable
fun MapArea(
    pins: List<MapPin>,
    onPinClick: (MapPin) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
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
                        Color(0xFFF0FDF4), // from-green-50
                        Color(0xFFEFF6FF), // via-blue-50
                        Color(0xFFF0FDF4)  // to-green-100
                    ),
                    radius = 1000f
                )
            )
    ) {
        // Mock map background with street lines
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeColor = Color(0xFF94A3B8).copy(alpha = 0.3f)
            
            // Horizontal lines
            drawLine(
                color = strokeColor,
                start = Offset(0f, size.height * 0.25f),
                end = Offset(size.width, size.height * 0.3f),
                strokeWidth = 4f
            )
            drawLine(
                color = strokeColor,
                start = Offset(0f, size.height * 0.5f),
                end = Offset(size.width, size.height * 0.5f),
                strokeWidth = 6f
            )
            drawLine(
                color = strokeColor,
                start = Offset(0f, size.height * 0.75f),
                end = Offset(size.width, size.height * 0.7f),
                strokeWidth = 4f
            )
            
            // Vertical lines
            drawLine(
                color = strokeColor,
                start = Offset(size.width * 0.25f, 0f),
                end = Offset(size.width * 0.3f, size.height),
                strokeWidth = 4f
            )
            drawLine(
                color = strokeColor,
                start = Offset(size.width * 0.5f, 0f),
                end = Offset(size.width * 0.5f, size.height),
                strokeWidth = 6f
            )
            drawLine(
                color = strokeColor,
                start = Offset(size.width * 0.75f, 0f),
                end = Offset(size.width * 0.7f, size.height),
                strokeWidth = 4f
            )
        }
        
        // Map Pins
        pins.forEach { pin ->
            var scale by remember { mutableStateOf(1f) }
            
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { pin.coordinates.x.toDp() },
                        y = with(density) { pin.coordinates.y.toDp() }
                    )
                    .size(32.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(getPinColor(pin.type))
                    .clickable {
                        onPinClick(pin)
                        scale = 1.1f
                    }
                    .zIndex(5f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPinIcon(pin.type),
                    contentDescription = pin.name,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Reset scale after animation
            LaunchedEffect(scale) {
                if (scale > 1f) {
                    kotlinx.coroutines.delay(150)
                    scale = 1f
                }
            }
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

@Composable
fun FilterButton(
    type: TransportType,
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) RouteifyBlue else Color.White,
            contentColor = if (isActive) Color.White else TextSecondary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp
            )
        }
    }
}


fun getPinIcon(type: TransportType): ImageVector {
    return when (type) {
        TransportType.BUS -> Icons.Default.DirectionsBus
        TransportType.TRAIN -> Icons.Default.Train
        TransportType.TAXI -> Icons.Default.LocalTaxi
    }
}

fun getPinColor(type: TransportType): Color {
    return when (type) {
        TransportType.BUS -> Color(0xFF2563EB) // bg-blue-600
        TransportType.TRAIN -> Color(0xFF16A34A) // bg-green-600
        TransportType.TAXI -> Color(0xFFCA8A04) // bg-yellow-600
    }
}

@Preview(showBackground = true)
@Composable
fun LiveMapScreenPreview() {
    RouteifyTheme {
        LiveMapScreen()
    }
}
