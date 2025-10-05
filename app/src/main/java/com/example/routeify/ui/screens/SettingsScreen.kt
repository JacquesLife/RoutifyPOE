package com.example.routeify.ui.screens

import android.Manifest
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var darkModeEnabled by remember { mutableStateOf(false) }
    var autoSyncEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    fun isNotificationsAllowed(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun isLocationAllowed(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    var notificationsEnabled by remember { mutableStateOf(isNotificationsAllowed()) }
    var locationEnabled by remember { mutableStateOf(isLocationAllowed()) }

    // Refresh switches when coming back from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsEnabled = isNotificationsAllowed()
                locationEnabled = isLocationAllowed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }

    // Launcher for location permissions (both fine and coarse)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationEnabled = granted
    }

    // Launcher for notifications permission (Android 13+)
    val notificationsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
        if (!granted) {
            // Keep switch off if denied
            notificationsEnabled = false
        }
    }

    fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openAppNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            openAppDetailsSettings()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // General Settings Section
        Text(
            text = "General",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Enable push notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                notificationsEnabled = true
                            }
                        } else {
                            notificationsEnabled = false
                            // Show rationale dialog before deep-linking
                            showNotificationSettingsDialog = true
                        }
                    }
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                SettingSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Settings Section
        Text(
            text = "Privacy & Security",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingSwitchItem(
                    icon = Icons.Default.LocationOn,
                    title = "Location Services",
                    subtitle = "Allow app to access location",
                    checked = locationEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            locationEnabled = false
                            showLocationSettingsDialog = true
                        }
                    }
                )

        if (showNotificationSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationSettingsDialog = false },
                title = { Text("Turn off notifications") },
                text = { Text("To disable notifications, change the setting in system settings.") },
                confirmButton = {
                    TextButton(onClick = {
                        showNotificationSettingsDialog = false
                        openAppNotificationSettings()
                    }) { Text("Open Settings") }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationSettingsDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showLocationSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showLocationSettingsDialog = false },
                title = { Text("Turn off location access") },
                text = { Text("To revoke location access, change the permission in system settings.") },
                confirmButton = {
                    TextButton(onClick = {
                        showLocationSettingsDialog = false
                        openAppDetailsSettings()
                    }) { Text("Open Settings") }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationSettingsDialog = false }) { Text("Cancel") }
                }
            )
        }

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                SettingSwitchItem(
                    icon = Icons.Default.Sync,
                    title = "Auto Sync",
                    subtitle = "Sync data automatically",
                    checked = autoSyncEnabled,
                    onCheckedChange = { autoSyncEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Other Options Section
        Text(
            text = "Other",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingNavigationItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    value = "English",
                    onClick = { /* Handle language selection */ }
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                SettingNavigationItem(
                    icon = Icons.Default.Storage,
                    title = "Storage",
                    value = "Clear cache",
                    onClick = { /* Handle storage */ }
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                SettingNavigationItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    value = "Version 1.0.0",
                    onClick = { /* Handle about */ }
                )
            }
        }
    }
}

@Composable
fun SettingSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}