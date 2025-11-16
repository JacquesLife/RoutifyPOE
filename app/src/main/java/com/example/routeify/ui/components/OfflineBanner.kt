/*
 * ============================================================================
 * OFFLINE BANNER - Network Status Indicator
 * ============================================================================
 * 
 * Visual indicator for offline mode with sync status.
 * Shows when device is offline and displays sync information.
 * 
 * ============================================================================
 */

package com.example.routeify.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routeify.data.sync.SyncManager

@Composable
fun OfflineBanner(
    isOnline: Boolean,
    syncStatus: SyncManager.SyncStatus = SyncManager.SyncStatus.IDLE,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isOnline || syncStatus is SyncManager.SyncStatus.SYNCING,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    !isOnline -> MaterialTheme.colorScheme.surfaceVariant
                    syncStatus is SyncManager.SyncStatus.SYNCING -> MaterialTheme.colorScheme.primaryContainer
                    syncStatus is SyncManager.SyncStatus.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer
                    syncStatus is SyncManager.SyncStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon based on status
                Icon(
                    imageVector = when {
                        syncStatus is SyncManager.SyncStatus.SYNCING -> Icons.Default.Sync
                        syncStatus is SyncManager.SyncStatus.SUCCESS -> Icons.Default.CheckCircle
                        syncStatus is SyncManager.SyncStatus.ERROR -> Icons.Default.Error
                        !isOnline -> Icons.Default.CloudOff
                        else -> Icons.Default.Cloud
                    },
                    contentDescription = null,
                    tint = when {
                        !isOnline -> MaterialTheme.colorScheme.onSurfaceVariant
                        syncStatus is SyncManager.SyncStatus.SYNCING -> MaterialTheme.colorScheme.onPrimaryContainer
                        syncStatus is SyncManager.SyncStatus.SUCCESS -> MaterialTheme.colorScheme.onTertiaryContainer
                        syncStatus is SyncManager.SyncStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            syncStatus is SyncManager.SyncStatus.SYNCING -> "Syncing Data..."
                            syncStatus is SyncManager.SyncStatus.SUCCESS -> "Synced Successfully"
                            syncStatus is SyncManager.SyncStatus.ERROR -> "Sync Failed"
                            !isOnline -> "Offline Mode"
                            else -> "Online"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            !isOnline -> MaterialTheme.colorScheme.onSurfaceVariant
                            syncStatus is SyncManager.SyncStatus.SYNCING -> MaterialTheme.colorScheme.onPrimaryContainer
                            syncStatus is SyncManager.SyncStatus.SUCCESS -> MaterialTheme.colorScheme.onTertiaryContainer
                            syncStatus is SyncManager.SyncStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    Text(
                        text = when (syncStatus) {
                            is SyncManager.SyncStatus.SYNCING -> "Updating your data"
                            is SyncManager.SyncStatus.SUCCESS -> "${syncStatus.itemsSynced} items synced"
                            is SyncManager.SyncStatus.ERROR -> syncStatus.message
                            else -> if (!isOnline) "Showing cached data. Will sync when online." else "Connected"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            !isOnline -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            syncStatus is SyncManager.SyncStatus.SYNCING -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            syncStatus is SyncManager.SyncStatus.SUCCESS -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            syncStatus is SyncManager.SyncStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }
                
                // Loading indicator when syncing
                if (syncStatus is SyncManager.SyncStatus.SYNCING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// Compact version for smaller spaces
@Composable
fun OfflineIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isOnline,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------
