/*
 * ============================================================================
 * GOOGLE FEATURES SCREEN - Location Services Interface
 * ============================================================================
 * 
 * Compose screen showcasing Google Maps Platform capabilities.
 * Provides UI for distance calculations, geocoding, and place operations.
 * 
 * UPDATED: All hardcoded strings replaced with string resources
 *
 * ============================================================================
 */

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.R
import com.example.routeify.data.model.TransitStop
import com.example.routeify.presentation.viewmodel.GoogleFeaturesViewModel

// Main screen for Google Features
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Main composable function for Google Features screen
fun GoogleFeaturesScreen(
    viewModel: GoogleFeaturesViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onRouteSelectedNavigateToMap: (String) -> Unit = {},
    initialDestination: String? = null
) {
    // Simple state to manage navigation between features
    var selectedStop by remember { mutableStateOf<TransitStop?>(null) }
    var currentScreen by remember { mutableStateOf(if (initialDestination != null) "route_planner" else "main") }

    // Navigate between different feature screens
    when (currentScreen) {
        "route_planner" -> {
            // Route planner screen with navigation callback and initial destination
            RoutePlannerScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = "main" },
                initialDestination = initialDestination,
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
        // Screen for nearby transit stops with callbacks for navigation and stop selection
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
        // Screen for place details with back and directions callbacks
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
        // Main features overview screen
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

// Overview screen listing Google-powered features
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_back))
            }

            Text(
                text = stringResource(R.string.google_features_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feature Cards
        FeatureCard(
            title = stringResource(R.string.google_route_planner),
            description = stringResource(R.string.google_route_planner_desc),
            icon = Icons.Default.Directions,
            onClick = { onFeatureClick("route_planner") }
        )

        // Nearby Transit
        Spacer(modifier = Modifier.height(12.dp))

        // Nearby Transit
        FeatureCard(
            title = stringResource(R.string.google_nearby_transit),
            description = stringResource(R.string.google_nearby_transit_desc),
            icon = Icons.Default.DirectionsBus,
            onClick = { onFeatureClick("nearby_transit") }
        )

        // Smart Suggestions
        Spacer(modifier = Modifier.height(12.dp))

        // Smart Suggestions
        FeatureCard(
            title = stringResource(R.string.google_smart_suggestions),
            description = stringResource(R.string.google_smart_suggestions_desc),
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
                // Privacy notice content
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Privacy icon
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Privacy text
                Text(
                    text = stringResource(R.string.google_privacy_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Reusable card component for each feature
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Feature card content
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Feature card content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                // Icon background
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                // Icon
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
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                // Expand icon
                Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.content_description_open_feature),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------