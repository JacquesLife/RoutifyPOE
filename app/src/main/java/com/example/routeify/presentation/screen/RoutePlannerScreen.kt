package com.example.routeify.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.presentation.viewmodel.GoogleFeaturesViewModel
import  com.example.routeify.domain.model.RouteSuggestion
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerScreen(
    viewModel: GoogleFeaturesViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val travelTimes by viewModel.travelTimes
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val routeSuggestions by viewModel.routeSuggestions
    val bestRouteSuggestion by viewModel.bestRouteSuggestion

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                text = "🗺️ Route Planner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // From Location Input
        OutlinedTextField(
            value = fromLocation,
            onValueChange = { fromLocation = it },
            label = { Text("From") },
            placeholder = { Text("Enter any address or place name") },
            leadingIcon = {
                Icon(Icons.Default.MyLocation, contentDescription = "From")
            },
            trailingIcon = {
                if (fromLocation.isNotEmpty()) {
                    IconButton(onClick = { fromLocation = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // To Location Input
        OutlinedTextField(
            value = toLocation,
            onValueChange = { toLocation = it },
            label = { Text("To") },
            placeholder = { Text("Enter any address or destination") },
            leadingIcon = {
                Icon(Icons.Default.Place, contentDescription = "To")
            },
            trailingIcon = {
                if (toLocation.isNotEmpty()) {
                    IconButton(onClick = { toLocation = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (fromLocation.isNotEmpty() && toLocation.isNotEmpty()) {
                        planRoute(fromLocation, toLocation, viewModel)
                    }
                }
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Helpful text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Enter any address, landmark, or place name. Examples: '123 Main St', 'Eiffel Tower', 'JFK Airport'",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Location Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    fromLocation = "Cape Town City Centre"
                    toLocation = "V&A Waterfront"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("🏙️ City ↔ Waterfront")
            }

            OutlinedButton(
                onClick = {
                    fromLocation = "Cape Town City Centre"
                    toLocation = "Cape Town Airport"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("✈️ City ↔ Airport")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Plan Route Button
        Button(
            onClick = {
                keyboardController?.hide()
                planRoute(fromLocation, toLocation, viewModel)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && fromLocation.isNotEmpty() && toLocation.isNotEmpty()
        ) {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("Finding locations...")
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Text("Plan Route")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Travel Time Results
        if (travelTimes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Route Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    travelTimes.forEach { travelTime ->
                        TravelTimeCard(travelTime = travelTime)
                    }
                }
            }
        }

        // Best Route Suggestion
        if (routeSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Suggested Routes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    routeSuggestions.forEach { route ->
                        Text(
                            text = "Route ${route.routeId}: ${route.timeEst} mins, ${route.distance} km",
                            color = if (route == bestRouteSuggestion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (route == bestRouteSuggestion) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

    }
}

    private fun planRoute(
    from: String,
    to: String,
    viewModel: GoogleFeaturesViewModel
) {
    if (from.isNotEmpty() && to.isNotEmpty()) {
        // Use the new address-based routing
        viewModel.planRouteWithAddresses(from, to)
    }
}