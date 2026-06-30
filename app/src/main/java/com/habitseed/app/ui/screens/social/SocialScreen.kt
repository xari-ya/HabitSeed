package com.habitseed.app.ui.screens.social

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.habitseed.app.data.local.entity.FriendEntity
import com.habitseed.app.data.social.dto.FollowingDto
import com.habitseed.app.data.social.dto.PublicProfileDto
import com.habitseed.app.ui.components.PlantAssetMapper
import com.habitseed.app.ui.feedback.rememberHabitSeedHaptics
import com.habitseed.app.ui.theme.HabitSeedDimens

@Composable
fun SocialScreen(
    viewModel: SocialViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var friendUid by remember { mutableStateOf("") }
    val haptics = rememberHabitSeedHaptics()
    val demoFriends = remember(uiState.friends, uiState.selectedTab) {
        friendsForTab(uiState.friends, uiState.selectedTab)
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            if (message.isSocialSuccess()) {
                haptics.success()
            } else {
                haptics.warning()
            }
            snackbarHostState.showSnackbar(message)
        }
    }

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
                SocialHeader()
            }

            item {
                SocialTabs(
                    selectedTab = uiState.selectedTab,
                    onSelectTab = {
                        haptics.selection()
                        viewModel.selectTab(it)
                    }
                )
            }

            if (uiState.isGoogleSignedIn) {
                if (uiState.selectedTab == SocialTab.Leaderboard) {
                    item {
                        LeaderboardActionRow(
                            onRefresh = {
                                haptics.selection()
                                viewModel.refresh()
                            }
                        )
                    }
                } else {
                    item {
                        AddFriendCard(
                            onAddFriend = {
                                haptics.selection()
                                showAddFriendDialog = true
                            }
                        )
                    }
                }

                if (uiState.errorMessage != null) {
                    item {
                        SocialMessageCard(
                            title = "Could not refresh social data",
                            message = "Showing the latest available social data. Your local habits still work offline."
                        )
                    }
                }

                if (uiState.isLoading) {
                    item {
                        LoadingSocialCard()
                    }
                } else if (uiState.selectedTab == SocialTab.Leaderboard) {
                    if (uiState.leaderboard.isEmpty()) {
                        item {
                            SocialMessageCard(
                                title = "No leaderboard yet",
                                message = "Complete a habit after signing in and HabitSeed will publish your public garden summary."
                            )
                        }
                    } else {
                        itemsIndexed(uiState.leaderboard, key = { _, profile -> profile.uid }) { index, profile ->
                            LeaderboardProfileCard(
                                profile = profile,
                                rank = index + 1
                            )
                        }
                    }
                } else {
                    if (uiState.following.isEmpty()) {
                        item {
                            SocialMessageCard(
                                title = "No followed gardens yet",
                                message = "Add a friend by Firebase UID to see their public garden and send nudges."
                            )
                        }
                    } else {
                        itemsIndexed(uiState.following, key = { _, friend -> friend.targetUid }) { index, friend ->
                            FollowingCard(
                                friend = friend,
                                rank = index + 1,
                                isNudgeInFlight = uiState.nudgingTargetUid == friend.targetUid,
                                onNudge = {
                                    haptics.selection()
                                    viewModel.sendNudge(friend)
                                },
                                onUnfollow = {
                                    haptics.selection()
                                    viewModel.unfollow(friend)
                                }
                            )
                        }
                    }
                }
            } else {
                item {
                    SocialMessageCard(
                        title = "Sign in with Google to connect with friends",
                        message = "Leaderboard, Add Friend, and nudges use Firebase. The preview below is local demo data."
                    )
                }

                if (demoFriends.isEmpty()) {
                    item {
                        SocialMessageCard(
                            title = "No demo friends to show yet",
                            message = "Mock garden friends will appear here once the local seed data is available."
                        )
                    }
                } else {
                    itemsIndexed(demoFriends, key = { _, friend -> friend.id }) { index, friend ->
                        FriendCard(
                            friend = friend,
                            rank = index + 1,
                            mode = uiState.selectedTab,
                            onNudge = {
                                haptics.selection()
                                viewModel.sendNudge(friend)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            uid = friendUid,
            onUidChange = { friendUid = it },
            onDismiss = {
                haptics.selection()
                showAddFriendDialog = false
            },
            onAddFriend = {
                haptics.selection()
                viewModel.addFriendByUid(friendUid)
                friendUid = ""
                showAddFriendDialog = false
            }
        )
    }
}

private fun String.isSocialSuccess(): Boolean {
    return this == "Friend added." ||
        this == "Friend removed." ||
        startsWith("Nudge sent")
}

@Composable
private fun SocialHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Friends' Gardens",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Stay accountable with a small circle of gentle growers.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "See public streak summaries, browse friendly gardens, and send a quick nudge when someone needs momentum.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SocialTabs(
    selectedTab: SocialTab,
    onSelectTab: (SocialTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SocialTab.entries.forEach { tab ->
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = selectedTab == tab,
                onClick = { onSelectTab(tab) },
                label = {
                    Text(
                        text = tab.label,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedTab == tab,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun AddFriendCard(
    onAddFriend: () -> Unit
) {
    Button(
        onClick = onAddFriend,
        modifier = Modifier
            .fillMaxWidth()
            .height(HabitSeedDimens.ButtonHeight),
        shape = RoundedCornerShape(HabitSeedDimens.ButtonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Icon(
            imageVector = Icons.Filled.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text("Add Friend", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LeaderboardActionRow(
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Global garden leaderboard",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Refresh whenever you want a fresh pull from Firebase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh leaderboard",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFriendDialog(
    uid: String,
    onUidChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddFriend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Friend",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter a friend's Firebase UID. HabitSeed will only read their public garden profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = uid,
                    onValueChange = onUidChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Firebase UID") },
                    shape = RoundedCornerShape(18.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAddFriend) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LoadingSocialCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
            Text(
                text = "Loading social garden...",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SocialMessageCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Groups,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LeaderboardProfileCard(
    profile: PublicProfileDto,
    rank: Int
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RemoteAvatarBadge(
                    name = profile.displayName,
                    photoUrl = profile.photoUrl,
                    rank = rank
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.displayName.ifBlank { "Gardener" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Level ${profile.gardenLevel} · ${profile.gardenLevelTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StreakPill(streak = profile.currentStreak)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = PlantAssetMapper.imageFor(
                                plantTypeId = profile.highestPlantTypeId,
                                growthStage = profile.highestPlantGrowthStage
                            )
                        ),
                        contentDescription = "${profile.displayName}'s top plant",
                        modifier = Modifier.size(58.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FriendStatRow(
                        icon = Icons.Filled.LocalFlorist,
                        text = displayPlantName(profile.highestPlantTypeId)
                    )
                    FriendStatRow(
                        icon = Icons.Filled.WaterDrop,
                        text = "${profile.gardenLevelProgressPercent}% to next level"
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PublicStat(
                    label = "Level",
                    value = profile.gardenLevel.toString(),
                    modifier = Modifier.weight(1f)
                )
                PublicStat(
                    label = "Weekly",
                    value = "${profile.weeklyCompletionRate.toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                PublicStat(
                    label = "Plants",
                    value = profile.fullyGrownPlants.toString(),
                    modifier = Modifier.weight(1f)
                )
                PublicStat(
                    label = "Done",
                    value = profile.totalCompletions.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FollowingCard(
    friend: FollowingDto,
    rank: Int,
    isNudgeInFlight: Boolean,
    onNudge: () -> Unit,
    onUnfollow: () -> Unit
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RemoteAvatarBadge(
                    name = friend.displayNameSnapshot,
                    photoUrl = friend.photoUrlSnapshot,
                    rank = rank
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = friend.displayNameSnapshot.ifBlank { "Gardener" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Level ${friend.gardenLevelSnapshot} · ${friend.gardenLevelTitleSnapshot}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StreakPill(streak = friend.currentStreakSnapshot)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = PlantAssetMapper.imageFor(
                                plantTypeId = friend.highestPlantTypeIdSnapshot,
                                growthStage = friend.highestPlantGrowthStageSnapshot
                            )
                        ),
                        contentDescription = "${friend.displayNameSnapshot}'s top plant",
                        modifier = Modifier.size(58.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FriendStatRow(
                        icon = Icons.Filled.LocalFlorist,
                        text = displayPlantName(friend.highestPlantTypeIdSnapshot)
                    )
                    FriendStatRow(
                        icon = Icons.Filled.WaterDrop,
                        text = "${friend.weeklyCompletionRateSnapshot.toInt()}% weekly · ${friend.fullyGrownPlantsSnapshot} grown"
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNudge,
                    enabled = !isNudgeInFlight,
                    modifier = Modifier
                        .weight(1f)
                        .height(HabitSeedDimens.ButtonHeight),
                    shape = RoundedCornerShape(HabitSeedDimens.ButtonRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (isNudgeInFlight) "Sending" else "Nudge",
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = onUnfollow,
                    modifier = Modifier
                        .weight(1f)
                        .height(HabitSeedDimens.ButtonHeight),
                    shape = RoundedCornerShape(HabitSeedDimens.ButtonRadius)
                ) {
                    Text("Unfollow", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PublicStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RemoteAvatarBadge(
    name: String,
    photoUrl: String?,
    rank: Int
) {
    Box {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsFor(name),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "$name avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "#$rank",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FriendCard(
    friend: FriendEntity,
    rank: Int,
    mode: SocialTab,
    onNudge: () -> Unit
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RemoteAvatarBadge(
                    name = friend.name,
                    photoUrl = null,
                    rank = rank
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = friend.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (mode == SocialTab.Leaderboard) {
                            "#$rank on the preview leaderboard"
                        } else {
                            gardenStatus(friend)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StreakPill(streak = friend.currentStreak)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = PlantAssetMapper.imageFor(
                                plantTypeId = friend.highestPlantAssetName,
                                growthStage = 5
                            )
                        ),
                        contentDescription = "${friend.name}'s top plant",
                        modifier = Modifier.size(58.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FriendStatRow(
                        icon = Icons.Filled.LocalFlorist,
                        text = displayPlantName(friend.highestPlantAssetName)
                    )
                    FriendStatRow(
                        icon = Icons.Filled.WaterDrop,
                        text = lastActiveLabel(friend.lastActiveDateKey)
                    )
                }
            }

            Button(
                onClick = onNudge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HabitSeedDimens.ButtonHeight),
                shape = RoundedCornerShape(HabitSeedDimens.ButtonRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Nudge",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StreakPill(streak: Int) {
    Surface(
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${friendshipStreak(streak)} streak",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FriendStatRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun friendsForTab(
    friends: List<FriendEntity>,
    selectedTab: SocialTab
): List<FriendEntity> {
    return when (selectedTab) {
        SocialTab.Leaderboard -> friends.sortedByDescending { it.currentStreak }
        SocialTab.Gardens -> friends.sortedBy { it.name.lowercase() }
    }
}

private fun initialsFor(name: String): String {
    return name
        .split(" ")
        .mapNotNull { part -> part.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
        .ifBlank { "G" }
}

private fun shortUid(uid: String): String {
    return if (uid.length <= 12) uid else "${uid.take(6)}...${uid.takeLast(4)}"
}

private fun gardenStatus(friend: FriendEntity): String {
    return if (friend.currentStreak >= 10) {
        "Thriving garden"
    } else if (friend.currentStreak >= 5) {
        "Steady growth"
    } else {
        "Fresh sprouts"
    }
}

private fun displayPlantName(assetName: String?): String {
    return when (assetName?.lowercase()?.removePrefix("plant_")) {
        "sunflower", "succulent" -> "Sunflower"
        "cactus", "desert_cactus" -> "Cactus"
        "lotus" -> "Lotus"
        "water_lily" -> "Water Lily"
        "bonsai", "small_bonsai", "golden_bonsai" -> "Bonsai"
        "lavender" -> "Lavender"
        "mushroom_garden" -> "Mushroom Garden"
        "venus_flytrap" -> "Venus Flytrap"
        "sakura_tree", "sakura_bonsai" -> "Sakura Tree"
        "starter_fern", "plant_starter_fern" -> "Starter Fern"
        "monstera", "monstera_deliciosa" -> "Monstera"
        else -> "Sunflower"
    }
}

private fun lastActiveLabel(lastActiveDateKey: String?): String {
    return if (lastActiveDateKey.isNullOrBlank()) {
        "Last active recently"
    } else {
        "Active on $lastActiveDateKey"
    }
}

private fun friendshipStreak(streak: Int): String {
    val unit = if (streak == 1) "day" else "days"
    return "$streak $unit"
}
