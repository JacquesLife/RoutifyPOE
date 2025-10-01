package com.example.routeify.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routeify.components.BottomNavigationBar
import com.example.routeify.ui.theme.*

data class RecentDestination(
    val name: String,
    val type: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRouteOptions: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    
    val recentDestinations = listOf(
        RecentDestination("Claremont Station", "Train Station", Icons.Default.Train),
        RecentDestination("V&A Waterfront", "Shopping Centre", Icons.Default.ShoppingCart),
        RecentDestination("University of Cape Town", "University", Icons.Default.School),
        RecentDestination("Cape Town International Airport", "Airport", Icons.Default.Flight),
        RecentDestination("Table Mountain", "Tourist Attraction", Icons.Default.Landscape)
    )
    
    fun handleSearch() {
        if (searchText.trim().isNotEmpty()) {
            // Navigate to route options with search query
            onNavigateToRouteOptions()
        }
    }
    
    fun handleDestinationSelect(destination: String) {
        // Navigate to route options with selected destination
        onNavigateToRouteOptions()
    }
    
    fun handleUseMyLocation() {
        // Navigate to route options with current location
        onNavigateToRouteOptions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header with gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            GradientStart,
                            GradientEnd
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 40.dp)
        ) {
            Column {
                // App Logo and Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Routeify Logo",
                            modifier = Modifier.size(20.dp),
                            tint = RouteifyBlue
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Routeify",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Search Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { 
                            Text(
                                "Where to?",
                                color = TextSecondary
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { handleSearch() }
                        )
                    )
                    
                    // Location Button
                    IconButton(
                        onClick = { handleUseMyLocation() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Use My Location",
                            tint = RouteifyBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        // Recent Destinations
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Recent Destinations",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentDestinations) { destination ->
                    RecentDestinationItem(
                        destination = destination,
                        onClick = { handleDestinationSelect(destination.name) }
                    )
                }
            }
        }
        
        // Bottom Navigation Placeholder
        Spacer(modifier = Modifier.weight(1f))
        
        BottomNavigationBar(
            onNavigateToHome = { /* Already on home */ },
            onNavigateToRouteOptions = onNavigateToRouteOptions,
            onNavigateToMap = onNavigateToMap,
            onNavigateToProfile = onNavigateToProfile,
            currentScreen = "home"
        )
    }
}

@Composable
fun RecentDestinationItem(
    destination: RecentDestination,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(RouteifyBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.type,
                    tint = RouteifyBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = destination.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = destination.type,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RouteifyTheme {
        HomeScreen(onNavigateToRouteOptions = {})
    }
}
