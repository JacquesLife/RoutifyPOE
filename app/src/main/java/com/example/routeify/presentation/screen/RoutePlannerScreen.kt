package com.example.routeify.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.domain.model.PlaceSuggestion
import com.example.routeify.domain.model.RouteSegment
import com.example.routeify.domain.model.TransitRoute
import com.example.routeify.presentation.viewmodel.GoogleFeaturesViewModel

data class PresetLocation(
    val id: String,
    val name: String,
    val address: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerScreen(
    viewModel: GoogleFeaturesViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onRouteSelected: (TransitRoute) -> Unit = {}
) {
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }
    var selectedFromPlace by remember { mutableStateOf<PlaceSuggestion?>(null) }
    var selectedToPlace by remember { mutableStateOf<PlaceSuggestion?>(null) }

    // Recent searches state (in a real app, this would be stored in DataStore)
    var recentSearches by remember { mutableStateOf(listOf("Cape Town Airport", "V&A Waterfront", "Canal Walk")) }

    val keyboardController = LocalSoftwareKeyboardController.current

    // Preset locations
    val presetLocations = remember {
        listOf(
            PresetLocation("home", "Home", "Your home address", Icons.Default.Home),
            PresetLocation("work", "Work", "Your workplace", Icons.Default.Work)
        )
    }

    val fromSuggestions by viewModel.fromSuggestions
    val toSuggestions by viewModel.toSuggestions
    val transitRoutes by viewModel.transitRoutes
    val isLoadingRoutes by viewModel.isLoadingRoutes
    val isLoadingFromSuggestions by viewModel.isLoadingFromSuggestions
    val isLoadingToSuggestions by viewModel.isLoadingToSuggestions
    val errorMessage by viewModel.errorMessage

    LaunchedEffect(fromLocation) {
        if (fromLocation.isNotEmpty() && selectedFromPlace?.description != fromLocation) {
            viewModel.getFromAutocompleteSuggestions(fromLocation)
            showFromDropdown = true
        }
    }

    LaunchedEffect(toLocation) {
        if (toLocation.isNotEmpty() && selectedToPlace?.description != toLocation) {
            viewModel.getToAutocompleteSuggestions(toLocation)
            showToDropdown = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fromLocation,
                    onValueChange = {
                        fromLocation = it
                        selectedFromPlace = null
                    },
                    label = { Text("From") },
                    placeholder = { Text("Enter starting location") },
                    leadingIcon = {
                        Icon(Icons.Default.MyLocation, contentDescription = "From")
                    },
                    trailingIcon = {
                        Row {
                            if (isLoadingFromSuggestions) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else if (fromLocation.isNotEmpty()) {
                                IconButton(onClick = {
                                    fromLocation = ""
                                    selectedFromPlace = null
                                    viewModel.clearFromSuggestions()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                AnimatedVisibility(
                    visible = showFromDropdown && (fromSuggestions.isNotEmpty() || (fromLocation.isNotEmpty() && !isLoadingFromSuggestions)),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(fromSuggestions) { suggestion ->
                                SuggestionItem(
                                    suggestion = suggestion,
                                    searchQuery = fromLocation,
                                    onClick = {
                                        fromLocation = suggestion.description
                                        selectedFromPlace = suggestion
                                        showFromDropdown = false
                                        viewModel.clearFromSuggestions()
                                    }
                                )
                            }
                            
                            // Show "Use this exact address" when no suggestions but user has typed something
                            if (fromSuggestions.isEmpty() && fromLocation.isNotEmpty() && !isLoadingFromSuggestions) {
                                item {
                                    ExactAddressItem(
                                        address = fromLocation,
                                        onClick = {
                                            selectedFromPlace = null
                                            showFromDropdown = false
                                            viewModel.clearFromSuggestions()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Preset chips for "From" field
            if (fromLocation.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PresetChipsRow(
                    presets = presetLocations,
                    recentSearches = recentSearches,
                    onPresetClick = { address ->
                        fromLocation = address
                        selectedFromPlace = null
                        showFromDropdown = false
                        viewModel.clearFromSuggestions()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = fromLocation
                        fromLocation = toLocation
                        toLocation = temp

                        val tempPlace = selectedFromPlace
                        selectedFromPlace = selectedToPlace
                        selectedToPlace = tempPlace
                    }
                ) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = "Swap locations",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = toLocation,
                    onValueChange = {
                        toLocation = it
                        selectedToPlace = null
                    },
                    label = { Text("To") },
                    placeholder = { Text("Enter destination") },
                    leadingIcon = {
                        Icon(Icons.Default.Place, contentDescription = "To")
                    },
                    trailingIcon = {
                        Row {
                            if (isLoadingToSuggestions) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else if (toLocation.isNotEmpty()) {
                                IconButton(onClick = {
                                    toLocation = ""
                                    selectedToPlace = null
                                    viewModel.clearToSuggestions()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                AnimatedVisibility(
                    visible = showToDropdown && (toSuggestions.isNotEmpty() || (toLocation.isNotEmpty() && !isLoadingToSuggestions)),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(toSuggestions) { suggestion ->
                                SuggestionItem(
                                    suggestion = suggestion,
                                    searchQuery = toLocation,
                                    onClick = {
                                        toLocation = suggestion.description
                                        selectedToPlace = suggestion
                                        showToDropdown = false
                                        viewModel.clearToSuggestions()
                                    }
                                )
                            }
                            
                            // Show "Use this exact address" when no suggestions but user has typed something
                            if (toSuggestions.isEmpty() && toLocation.isNotEmpty() && !isLoadingToSuggestions) {
                                item {
                                    ExactAddressItem(
                                        address = toLocation,
                                        onClick = {
                                            selectedToPlace = null
                                            showToDropdown = false
                                            viewModel.clearToSuggestions()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Preset chips for "To" field
            if (toLocation.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PresetChipsRow(
                    presets = presetLocations,
                    recentSearches = recentSearches,
                    onPresetClick = { address ->
                        toLocation = address
                        selectedToPlace = null
                        showToDropdown = false
                        viewModel.clearToSuggestions()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        fromLocation = "Cape Town City Centre"
                        toLocation = "V&A Waterfront"
                        selectedFromPlace = null
                        selectedToPlace = null
                        
                        // Add to recent searches
                        val newRecentSearches = (listOf(fromLocation, toLocation) + recentSearches)
                            .distinct()
                            .take(5)
                        recentSearches = newRecentSearches
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🏙️ City → Waterfront", style = MaterialTheme.typography.bodySmall)
                }

                OutlinedButton(
                    onClick = {
                        fromLocation = "Cape Town City Centre"
                        toLocation = "Cape Town Airport"
                        selectedFromPlace = null
                        selectedToPlace = null
                        
                        // Add to recent searches
                        val newRecentSearches = (listOf(fromLocation, toLocation) + recentSearches)
                            .distinct()
                            .take(5)
                        recentSearches = newRecentSearches
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("✈️ City → Airport", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    showFromDropdown = false
                    showToDropdown = false
                    
                    // Add to recent searches if not already present
                    val newRecentSearches = (listOf(fromLocation, toLocation) + recentSearches)
                        .distinct()
                        .take(5) // Keep only 5 most recent
                    recentSearches = newRecentSearches
                    
                    viewModel.getTransitRoutes(fromLocation, toLocation)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoadingRoutes && fromLocation.isNotEmpty() && toLocation.isNotEmpty()
            ) {
                if (isLoadingRoutes) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text("Finding routes...")
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Text("Find Transit Routes")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (transitRoutes.isNotEmpty()) {
                Text(
                    text = "Available Routes (${transitRoutes.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                transitRoutes.forEachIndexed { index, route ->
                    TransitRouteCard(
                        route = route,
                        routeNumber = index + 1,
                        onClick = { onRouteSelected(route) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (transitRoutes.isEmpty() && !isLoadingRoutes && errorMessage == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "How to use:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Type to see location suggestions\n• Select from dropdown or continue typing\n• Click 'Find Transit Routes' to see bus and train options",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChipsRow(
    presets: List<PresetLocation>,
    recentSearches: List<String>,
    onPresetClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        // Preset locations (Home, Work)
        items(presets) { preset ->
            FilterChip(
                onClick = { onPresetClick(preset.address) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            preset.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(preset.name)
                    }
                },
                selected = false,
                leadingIcon = null
            )
        }
        
        // Recent searches
        items(recentSearches) { search ->
            FilterChip(
                onClick = { onPresetClick(search) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(search)
                    }
                },
                selected = false,
                leadingIcon = null
            )
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: PlaceSuggestion,
    searchQuery: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    highlightMatchedText(suggestion.mainText, searchQuery)
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (suggestion.secondaryText.isNotEmpty()) {
                Text(
                    text = buildAnnotatedString {
                        highlightMatchedText(suggestion.secondaryText, searchQuery)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Divider()
}

@Composable
private fun ExactAddressItem(
    address: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Use this exact address",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Divider()
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightMatchedText(
    text: String,
    query: String
) {
    if (query.isEmpty()) {
        append(text)
        return
    }
    
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    var startIndex = 0
    
    while (true) {
        val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
        if (matchIndex == -1) {
            // No more matches, append remaining text
            if (startIndex < text.length) {
                append(text.substring(startIndex))
            }
            break
        }
        
        // Append text before match
        if (matchIndex > startIndex) {
            append(text.substring(startIndex, matchIndex))
        }
        
        // Append highlighted match
        withStyle(style = SpanStyle(background = Color.Yellow.copy(alpha = 0.3f))) {
            append(text.substring(matchIndex, matchIndex + query.length))
        }
        
        startIndex = matchIndex + query.length
    }
}

@Composable
private fun TransitRouteCard(
    route: TransitRoute,
    routeNumber: Int,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "$routeNumber",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = getTransitIcon(route.primaryTransitMode),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = route.totalDuration,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = route.totalDistance,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            route.segments.forEachIndexed { index, segment ->
                RouteSegmentItem(
                    segment = segment,
                    isLast = index == route.segments.lastIndex
                )
                if (index < route.segments.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RouteSegmentItem(
    segment: RouteSegment,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            if (segment.transitInfo != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = segment.transitInfo.getColorInt()?.let { Color(it) }
                        ?: MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = segment.transitInfo.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Icon(
                    Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            if (segment.transitInfo != null) {
                val info = segment.transitInfo

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = getTransitIcon(info.vehicleType),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = info.lineName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "From: ${info.departureStop}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "To: ${info.arrivalStop}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${info.numStops} stops • ${segment.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                info.headsign?.let {
                    Text(
                        text = "→ $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                Text(
                    text = segment.instruction,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${segment.distance} • ${segment.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getTransitIcon(vehicleType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (vehicleType.uppercase()) {
        "BUS" -> Icons.Default.DirectionsBus
        "TRAIN", "HEAVY_RAIL", "RAIL" -> Icons.Default.Train
        "SUBWAY", "METRO_RAIL" -> Icons.Default.Subway
        "TRAM", "LIGHT_RAIL" -> Icons.Default.Tram
        "WALKING" -> Icons.Default.DirectionsWalk
        else -> Icons.Default.DirectionsTransit
    }
}