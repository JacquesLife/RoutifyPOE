package com.example.routeify.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.routeify.domain.model.RouteSuggestion
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun SimpleSmartSuggestionCard(
    suggestion: RouteSuggestion,
    routeNumber: Int,
    modifier: Modifier = Modifier,
) {
    val isRecommended = suggestion.recommended

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Route $routeNumber",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if(isRecommended){
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Best Option",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Recommended",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time and Distance
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${suggestion.timeEst} minutes",
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${suggestion.distance} km",
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transport Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Transport: ${suggestion.transportType}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if(suggestion.isQuickRoute){
                    Text(
                        text = "Quick Route",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if(suggestion.hasTransfer){
                    Text(
                        text = "Includes Transfer",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Direct Route",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            //Reliability Score
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
               val reliabilityColor = when(suggestion.reliabilityScore){
                   "Excellent" -> Color(0xFF4CAF50) // Green
                   "Good" -> Color(0xFFFFC107) // Amber
                   else -> Color(0xFFF44336) // Red
                }
                Icon(
                 Icons.Default.Speed,
                 contentDescription = null,
                 tint = reliabilityColor,
                 modifier = Modifier.size(16.dp)
                )
                Text(
                    "Reliability: ${suggestion.reliabilityScore}",
                    color = reliabilityColor, fontSize = 14.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}