package com.habitseed.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.habitseed.app.ui.feedback.rememberHabitSeedHaptics
import com.habitseed.app.ui.feedback.rememberNotificationPermissionRequester
import com.habitseed.app.ui.theme.DarkSlate
import com.habitseed.app.ui.theme.HabitSeedDimens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val haptics = rememberHabitSeedHaptics()
    val requestNotificationPermission = rememberNotificationPermissionRequester(
        onGranted = {
            haptics.selection()
            viewModel.toggleNotifications(true)
        },
        onDenied = {
            haptics.warning()
            viewModel.showMessage("Allow notifications to receive habit reminders.")
        }
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                ProfileEvent.Logout -> onLogout()
                ProfileEvent.ProfileSaved -> Unit
            }
        }
    }

    val user = uiState.user
    val settings = uiState.settings

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = HabitSeedDimens.ScreenPadding),
            contentPadding = PaddingValues(vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileHeader(
                    name = user?.name ?: "Gardener",
                    email = user?.email ?: "Email not shared",
                    avatarUrl = user?.avatarUrl,
                    providerLabel = if (user?.authProvider == "google") "Google" else "Local",
                    joinedAt = user?.joinedAt ?: System.currentTimeMillis()
                )
            }

            item {
                ProfileSummaryCard(
                    waterDrops = user?.waterDrops ?: 0,
                    currentStreak = user?.currentStreak ?: 0,
                    bestStreak = user?.bestStreak ?: 0
                )
            }

            item {
                SettingsGroupCard(
                    title = "Account",
                    rows = listOf(
                        SettingRowAction(
                            icon = Icons.Filled.Edit,
                            title = "Edit Profile",
                            subtitle = "Update your name and profile image",
                            onClick = {
                                haptics.selection()
                                onEditProfile()
                            }
                        )
                    )
                )
            }

            item {
                FirebaseUidCard(
                    firebaseUid = user?.firebaseUid,
                    clipboardManager = clipboardManager,
                    onCopy = { message ->
                        haptics.success()
                        viewModel.showMessage(message)
                    }
                )
            }

            item {
                AppSettingsCard(
                    notificationsEnabled = settings?.notificationsEnabled ?: true,
                    darkModeEnabled = settings?.darkModeEnabled ?: false,
                    soundEnabled = settings?.soundEnabled ?: true,
                    hapticsEnabled = settings?.hapticsEnabled ?: true,
                    reminderTime = formatReminderTime(
                        hour = settings?.reminderHour ?: 8,
                        minute = settings?.reminderMinute ?: 0
                    ),
                    onToggleNotifications = { enabled ->
                        if (enabled) {
                            requestNotificationPermission()
                        } else {
                            haptics.selection()
                            viewModel.toggleNotifications(false)
                        }
                    },
                    onToggleDarkMode = {
                        haptics.selection()
                        viewModel.toggleDarkMode(it)
                    },
                    onToggleSound = {
                        haptics.selection()
                        viewModel.toggleSound(it)
                    },
                    onToggleHaptics = {
                        haptics.selection()
                        viewModel.toggleHaptics(it)
                    },
                    onReminderClick = {
                        haptics.selection()
                        viewModel.cycleReminderTime()
                    }
                )
            }

            item {
                SettingsGroupCard(
                    title = "Session",
                    rows = listOf(
                        SettingRowAction(
                            icon = Icons.Filled.Lock,
                            title = "Log Out",
                            subtitle = if (uiState.isLoggingOut) {
                                "Backing up your data..."
                            } else {
                                "Return to the login screen"
                            },
                            tint = MaterialTheme.colorScheme.error,
                            titleColor = MaterialTheme.colorScheme.error,
                            enabled = !uiState.isLoggingOut,
                            onClick = {
                                haptics.selection()
                                viewModel.logout()
                            }
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    avatarUrl: String?,
    providerLabel: String,
    joinedAt: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "$name avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Signed in with $providerLabel",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Joined ${formatJoinDate(joinedAt)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ProfileSummaryCard(
    waterDrops: Int,
    currentStreak: Int,
    bestStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryStat(
                label = "Drops",
                value = waterDrops.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryStat(
                label = "Current Streak",
                value = currentStreak.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryStat(
                label = "Best Streak",
                value = bestStreak.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
        )
    }
}

private data class SettingRowAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val tint: Color = Color.Unspecified,
    val titleColor: Color = DarkSlate,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

@Composable
private fun resolvedRowTint(row: SettingRowAction): Color {
    return if (row.tint == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else if (row.tint == MaterialTheme.colorScheme.error) {
        MaterialTheme.colorScheme.error
    } else {
        row.tint
    }
}

@Composable
private fun resolvedRowTitleColor(row: SettingRowAction): Color {
    return if (row.titleColor == MaterialTheme.colorScheme.error) {
        MaterialTheme.colorScheme.error
    } else {
        row.titleColor
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    rows: List<SettingRowAction>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            rows.forEachIndexed { index, row ->
                ActionRow(row = row)
                if (index != rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    row: SettingRowAction
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = row.enabled, onClick = row.onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = row.icon,
            contentDescription = null,
            tint = resolvedRowTint(row),
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.labelLarge,
                color = resolvedRowTitleColor(row),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = row.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = if (row.titleColor == MaterialTheme.colorScheme.error) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun FirebaseUidCard(
    firebaseUid: String?,
    clipboardManager: ClipboardManager,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Firebase UID",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = firebaseUid ?: "Not linked yet",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = {
                        if (!firebaseUid.isNullOrBlank()) {
                            clipboardManager.setText(AnnotatedString(firebaseUid))
                            onCopy("Firebase UID copied")
                        }
                    },
                    enabled = !firebaseUid.isNullOrBlank()
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy Firebase UID",
                        tint = if (firebaseUid.isNullOrBlank()) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSettingsCard(
    notificationsEnabled: Boolean,
    darkModeEnabled: Boolean,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    reminderTime: String,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onReminderClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "App",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            ToggleRow(
                icon = Icons.Filled.Notifications,
                title = "Notifications",
                subtitle = "Daily habit reminders",
                checked = notificationsEnabled,
                onCheckedChange = onToggleNotifications
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            ToggleRow(
                icon = Icons.Filled.DarkMode,
                title = "Dark Mode",
                subtitle = "Switch the app theme",
                checked = darkModeEnabled,
                onCheckedChange = onToggleDarkMode
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            ActionRow(
                row = SettingRowAction(
                    icon = Icons.Filled.Schedule,
                    title = "Reminder Time",
                    subtitle = reminderTime,
                    onClick = onReminderClick
                )
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            ToggleRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Sound",
                subtitle = "Audio feedback for actions",
                checked = soundEnabled,
                onCheckedChange = onToggleSound
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            ToggleRow(
                icon = Icons.Filled.Vibration,
                title = "Haptics",
                subtitle = "Physical feedback on interaction",
                checked = hapticsEnabled,
                onCheckedChange = onToggleHaptics
            )
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

private fun formatJoinDate(joinedAt: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    return Instant.ofEpochMilli(joinedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}

private fun formatReminderTime(hour: Int, minute: Int): String {
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val suffix = if (hour >= 12) "PM" else "AM"
    return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, suffix)
}
