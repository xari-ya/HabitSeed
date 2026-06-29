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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitseed.app.ui.theme.DarkSlate
import com.habitseed.app.ui.theme.ForestGreen
import com.habitseed.app.ui.theme.HabitSeedDimens
import com.habitseed.app.ui.theme.SoftRed
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                ProfileEvent.Logout -> onLogout()
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
                    name = user?.name ?: "Alex",
                    email = user?.email ?: "alex@example.com",
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
                            subtitle = "Update your identity details",
                            onClick = { viewModel.onMockAction("Edit Profile") }
                        ),
                        SettingRowAction(
                            icon = Icons.Filled.Lock,
                            title = "Change Password",
                            subtitle = "Demo-only security action",
                            onClick = { viewModel.onMockAction("Change Password") }
                        )
                    )
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
                    onToggleNotifications = viewModel::toggleNotifications,
                    onToggleDarkMode = viewModel::toggleDarkMode,
                    onToggleSound = viewModel::toggleSound,
                    onToggleHaptics = viewModel::toggleHaptics,
                    onReminderClick = viewModel::cycleReminderTime
                )
            }

            item {
                SettingsGroupCard(
                    title = "Support",
                    rows = listOf(
                        SettingRowAction(
                            icon = Icons.Filled.HelpOutline,
                            title = "FAQ",
                            subtitle = "Common guidance for using HabitSeed",
                            onClick = { viewModel.onMockAction("FAQ") }
                        ),
                        SettingRowAction(
                            icon = Icons.Filled.Phone,
                            title = "Contact Us",
                            subtitle = "Reach the project owner",
                            onClick = { viewModel.onMockAction("Contact Us") }
                        ),
                        SettingRowAction(
                            icon = Icons.Filled.Logout,
                            title = "Log Out",
                            subtitle = "Return to the login screen",
                            tint = MaterialTheme.colorScheme.error,
                            titleColor = MaterialTheme.colorScheme.error,
                            onClick = viewModel::logout
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
    joinedAt: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
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
    val tint: Color = ForestGreen,
    val titleColor: Color = DarkSlate,
    val onClick: () -> Unit
)

@Composable
private fun resolvedRowTint(row: SettingRowAction): Color {
    return if (row.tint == SoftRed || row.tint == MaterialTheme.colorScheme.error) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun resolvedRowTitleColor(row: SettingRowAction): Color {
    return if (row.titleColor == SoftRed || row.titleColor == MaterialTheme.colorScheme.error) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
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
                    Divider(
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
            .clickable(onClick = row.onClick)
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
            tint = if (row.titleColor == MaterialTheme.colorScheme.error || row.titleColor == SoftRed) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
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
            Divider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            ToggleRow(
                icon = Icons.Filled.DarkMode,
                title = "Dark Mode",
                subtitle = "Switch the app theme",
                checked = darkModeEnabled,
                onCheckedChange = onToggleDarkMode
            )
            Divider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            ActionRow(
                row = SettingRowAction(
                    icon = Icons.Filled.Schedule,
                    title = "Reminder Time",
                    subtitle = reminderTime,
                    onClick = onReminderClick
                )
            )
            Divider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            ToggleRow(
                icon = Icons.Filled.VolumeUp,
                title = "Sound",
                subtitle = "Audio feedback for actions",
                checked = soundEnabled,
                onCheckedChange = onToggleSound
            )
            Divider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.surfaceVariant)
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
