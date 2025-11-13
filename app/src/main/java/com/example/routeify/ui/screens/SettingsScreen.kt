/*
 * ============================================================================
 * SETTINGS SCREEN - App Configuration Interface
 * ============================================================================
 *
 * Compose screen for app preferences and configuration options.
 * Manages permissions, notifications, and user customization settings.
 *
 * ============================================================================
 */

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routeify.ui.theme.RouteifyBlue500
import com.example.routeify.R
import com.example.routeify.data.preferences.LanguageManager

// Settings screen composable
@Composable
fun SettingsScreen() {
    // Context and lifecycle
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val languageManager = remember { LanguageManager.getInstance(context) }

    var darkModeEnabled by remember { mutableStateOf(false) }
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Observe language changes
    val currentLanguage = languageManager.currentLanguage

    // Check if notifications are enabled
    fun isNotificationsAllowed(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    // Check if location permissions are granted
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

    // State for switches
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

    // Dialog state
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

    // Functions to open system settings
    fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // Open notification settings (different for Android O and above)
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

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Screen title
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // General Settings Section
        Text(
            text = stringResource(R.string.settings_general),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // General Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            // General settings content
            Column {
                SettingSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Enable push notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            // Request permission
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

                // Divider
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                // Dark Mode setting
                SettingSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = stringResource(R.string.settings_dark_mode_subtitle),
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Settings Section
        Text(
            text = stringResource(R.string.settings_privacy),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Privacy Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            // Privacy settings content
            Column {
                SettingSwitchItem(
                    icon = Icons.Default.LocationOn,
                    title = stringResource(R.string.settings_location),
                    subtitle = stringResource(R.string.settings_location_subtitle),
                    checked = locationEnabled,
                    // Request permissions or show dialog
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

        // Rationale dialog for notifications
        if (showNotificationSettingsDialog) {
            // Show rationale dialog
            AlertDialog(
                // Dismiss on outside touch or back press
                onDismissRequest = { showNotificationSettingsDialog = false },
                title = { Text(stringResource(R.string.dialog_notifications_title)) },
                text = { Text(stringResource(R.string.dialog_notifications_message)) },
                confirmButton = {
                    // Deep-link to app notification settings
                    TextButton(onClick = {
                        showNotificationSettingsDialog = false
                        openAppNotificationSettings()
                    }) { Text(stringResource(R.string.button_open_settings)) }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationSettingsDialog = false }) { Text(stringResource(R.string.button_cancel)) }
                }
            )
        }

        // Rationale dialog for location
        if (showLocationSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showLocationSettingsDialog = false },
                title = { Text(stringResource(R.string.dialog_location_title)) },
                text = { Text(stringResource(R.string.dialog_location_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showLocationSettingsDialog = false
                        openAppDetailsSettings()
                    }) { Text(stringResource(R.string.button_open_settings)) }
                },
                // Dismiss button
                dismissButton = {
                    TextButton(onClick = { showLocationSettingsDialog = false }) { Text(stringResource(R.string.button_cancel)) }
                }
            )
        }
                // Divider
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                SettingSwitchItem(
                    icon = Icons.Default.Sync,
                    title = stringResource(R.string.settings_auto_sync),
                    subtitle = stringResource(R.string.settings_auto_sync_subtitle),
                    checked = autoSyncEnabled,
                    onCheckedChange = { autoSyncEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Other Options Section
        Text(
            text = stringResource(R.string.settings_other),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Other Options Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            // Other options content
            Column {
                SettingNavigationItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language),
                    value = languageManager.getLanguageName(currentLanguage),
                    onClick = { showLanguageDialog = true }
                )

                // Divider
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                SettingNavigationItem(
                    icon = Icons.Default.Storage,
                    title = stringResource(R.string.settings_storage),
                    value = stringResource(R.string.settings_storage_value),
                    onClick = { /* Handle storage */ }
                )

                // Divider
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                // Language section Dialog
                if (showLanguageDialog) {
                    LanguageSectionDialog(
                        languageManager = languageManager,
                        currentLanguage = currentLanguage,
                        onDismiss = { showLanguageDialog = false }
                    )
                }

//                SettingNavigationItem(
//                    icon = Icons.Default.Info,
//                    title = "About",
//                    value = "Version 1.0.0",
//                    onClick = { /* Handle about */ }
//                )
            }
        }
    }
}

// Individual setting item with a switch
@Composable
fun SettingSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // Entire row is clickable
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = RouteifyBlue500,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Title and subtitle
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Switch
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = RouteifyBlue500
            )
        )
    }
}

// Individual setting item that navigates on click
@Composable
fun SettingNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    // Entire row is clickable
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icon and text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = RouteifyBlue500,
                modifier = Modifier.size(24.dp)
            )
            // Spacer
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Title and value
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                // Value text
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Chevron icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Language section dialog
@Composable
fun LanguageSectionDialog(languageManager: LanguageManager, currentLanguage: String, onDismiss: () -> Unit) {
    val languages = listOf(
        LanguageManager.ENGLISH to languageManager.getLanguageName(LanguageManager.ENGLISH),
        LanguageManager.AFRIKAANS to languageManager.getLanguageName(LanguageManager.AFRIKAANS)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_language_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == code,
                            onClick = {
                                languageManager.setLanguage(code)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = RouteifyBlue500
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_close))
            }
        }
    )
}

// --------------------------------------------------End of File----------------------------------------------------------------