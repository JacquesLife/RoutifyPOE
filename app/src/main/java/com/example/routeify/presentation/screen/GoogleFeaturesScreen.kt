package com.example.routeify.presentation.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.data.model.TransitStop
import com.example.routeify.presentation.viewmodel.GoogleFeaturesViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleFeaturesScreen(
    viewModel: GoogleFeaturesViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onRouteSelectedNavigateToMap: (String) -> Unit = {}
) {
    var selectedStop by remember { mutableStateOf<TransitStop?>(null) }
    var currentScreen by remember { mutableStateOf("main") }

    when (currentScreen) {
        "route_planner" -> {
            RoutePlannerScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = "main" },
                onRouteSelected = { route ->
                    val fromLat = route.startLocation?.latitude?.toString()
                    val fromLng = route.startLocation?.longitude?.toString()
                    val toLat = route.endLocation?.latitude?.toString()
                    val toLng = route.endLocation?.longitude?.toString()
                    val poly = route.overviewPolyline
                    val encodedPoly = poly?.let { java.net.URLEncoder.encode(it, "UTF-8") }
                    val routeStr = buildString {
                        append("map")
                        append("?")
                        if (fromLat != null) append("fromLat=$fromLat&")
                        if (fromLng != null) append("fromLng=$fromLng&")
                        if (toLat != null) append("toLat=$toLat&")
                        if (toLng != null) append("toLng=$toLng&")
                        if (encodedPoly != null) append("poly=$encodedPoly")
                    }.trimEnd('&')
                    onRouteSelectedNavigateToMap(routeStr)
                }
            )
        }
        "nearby_transit" -> {
            NearbyTransitScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = "main" },
                onStopClick = { stop ->
                    selectedStop = stop
                    currentScreen = "place_details"
                }
            )
        }
        "place_details" -> {
            selectedStop?.let { stop ->
                PlaceDetailsScreen(
                    stop = stop,
                    viewModel = viewModel,
                    onBackClick = { currentScreen = "nearby_transit" },
                    onDirectionsClick = { currentScreen = "route_planner" }
                )
            }
        }
        else -> {
            MainGoogleFeaturesScreen(
                onBackClick = onBackClick,
                onFeatureClick = { feature ->
                    currentScreen = feature
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainGoogleFeaturesScreen(
    onBackClick: () -> Unit,
    onFeatureClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = " Google Services",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

//        Text(
//            text = "Discover powerful transit and navigation features powered by Google APIs",
//            style = MaterialTheme.typography.bodyLarge,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            modifier = Modifier.padding(bottom = 24.dp)
//        )
        
        // Feature Cards
        FeatureCard(
            title = "🗺 Route Planner",
            description = "Plan your journey with real-time travel times and multiple transport options",
            icon = Icons.Default.Directions,
            onClick = { onFeatureClick("route_planner") }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        FeatureCard(
            title = " Nearby Transit",
            description = "Find transit stops, bus stations, and train stations near you",
            icon = Icons.Default.DirectionsBus,
            onClick = { onFeatureClick("nearby_transit") }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        FeatureCard(
            title = " Smart Suggestions",
            description = "Get personalized route recommendations based on your travel patterns",
            icon = Icons.Default.Psychology,
            onClick = { /* Navigate to smart suggestions */ }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        

        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Privacy Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Your privacy is protected. Location data is only used for transit planning.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open feature",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
