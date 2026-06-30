package com.habitseed.app.data.social

import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.local.dao.CachedFollowingProfileDao
import com.habitseed.app.data.local.dao.CachedLeaderboardProfileDao
import com.habitseed.app.data.local.dao.SocialCacheMetadataDao
import com.habitseed.app.data.local.entity.CachedFollowingProfileEntity
import com.habitseed.app.data.local.entity.CachedLeaderboardProfileEntity
import com.habitseed.app.data.local.entity.SocialCacheMetadataEntity
import com.habitseed.app.data.backup.BackupRepository
import com.habitseed.app.data.social.dto.FollowingDto
import com.habitseed.app.data.social.dto.NudgeDto
import com.habitseed.app.data.social.dto.NudgeMessageTypes
import com.habitseed.app.data.social.dto.PublicProfileDto
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.UserRepository
import com.habitseed.app.domain.social.PublicProfileSyncPolicy
import com.habitseed.app.domain.social.SocialSummaryMapper
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class PublicProfileSyncReason {
    SIGN_IN,
    APP_START,
    HABIT_COMPLETED,
    PROFILE_EDIT
}

data class PublicProfileSyncResult(
    val didWrite: Boolean,
    val reason: PublicProfileSyncReason,
    val message: String
)

class SocialSyncRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val remoteDataSource: SocialRemoteDataSource,
    private val cachedLeaderboardProfileDao: CachedLeaderboardProfileDao,
    private val cachedFollowingProfileDao: CachedFollowingProfileDao,
    private val socialCacheMetadataDao: SocialCacheMetadataDao,
    private val backupRepository: BackupRepository
) {
    fun observeCachedLeaderboard() = cachedLeaderboardProfileDao.observeProfiles()
        .map { profiles -> profiles.map { it.toDto() } }

    fun observeCachedFollowing() = cachedFollowingProfileDao.observeProfiles()
        .map { profiles -> profiles.map { it.toDto() } }

    suspend fun syncPublicProfile(
        reason: PublicProfileSyncReason,
        force: Boolean = false
    ): Result<PublicProfileSyncResult> {
        val authUser = authRepository.currentUser()
            ?: return Result.success(
                PublicProfileSyncResult(
                    didWrite = false,
                    reason = reason,
                    message = "No Firebase user is signed in."
                )
            )
        return runCatching {
            val localUser = userRepository.getUser().first()
                ?: throw SocialException("Local profile is not ready yet.")
            val habits = habitRepository.getAllHabits().first()
            val stats = habitRepository.getStatsForLast30Days().first()
            val now = System.currentTimeMillis()

            val publicProfile = SocialSummaryMapper.toPublicProfile(
                firebaseUid = authUser.uid,
                user = localUser,
                habits = habits,
                stats = stats,
                existingCreatedAt = localUser.createdAt,
                now = now
            )

            val publicProfileSyncHash = PublicProfileSyncPolicy.syncHash(publicProfile)
            val shouldSync = PublicProfileSyncPolicy.shouldSync(
                reason = reason,
                previousHash = localUser.publicProfileSyncHash,
                lastSyncedAt = localUser.lastCloudSyncAt,
                nextHash = publicProfileSyncHash,
                now = now
            )
            if (!force && !shouldSync) {
                return@runCatching PublicProfileSyncResult(
                    didWrite = false,
                    reason = reason,
                    message = "Public profile sync skipped."
                )
            }

            remoteDataSource.upsertPublicProfile(publicProfile)
            userRepository.updateLastCloudSyncAt(
                timestamp = now,
                publicProfileSyncHash = publicProfileSyncHash
            )
            PublicProfileSyncResult(
                didWrite = true,
                reason = reason,
                message = "Public profile synced."
            )
        }
    }

    suspend fun loadLeaderboard(): Result<List<PublicProfileDto>> {
        if (!authRepository.isSignedInWithGoogle()) {
            return Result.failure(SocialException("Sign in with Google to connect with friends."))
        }
        return runCatching {
            val now = System.currentTimeMillis()
            val leaderboard = remoteDataSource.getLeaderboard()
            cachedLeaderboardProfileDao.replaceAll(
                leaderboard.mapIndexed { index, profile ->
                    CachedLeaderboardProfileEntity.fromDto(
                        profile = profile,
                        rank = index + 1,
                        cachedAt = now
                    )
                }
            )
            socialCacheMetadataDao.upsert(SocialCacheMetadataEntity(LEADERBOARD_CACHE_KEY, now))
            leaderboard
        }
    }

    suspend fun loadFollowing(): Result<List<FollowingDto>> {
        val currentUid = authRepository.currentUser()?.uid
            ?: return Result.failure(SocialException("Sign in with Google to connect with friends."))
        return runCatching {
            val now = System.currentTimeMillis()
            val following = remoteDataSource.getFollowing(currentUid)
            cachedFollowingProfileDao.replaceAll(
                following.map { CachedFollowingProfileEntity.fromDto(it, now) }
            )
            socialCacheMetadataDao.upsert(SocialCacheMetadataEntity(FOLLOWING_CACHE_KEY, now))
            following
        }
    }

    suspend fun addFriendByUid(targetUid: String): Result<FollowingDto> {
        val cleanTargetUid = targetUid.trim()
        val currentUid = authRepository.currentUser()?.uid
            ?: return Result.failure(SocialException("Sign in with Google to add friends."))

        if (cleanTargetUid.isBlank()) {
            return Result.failure(SocialException("Enter a Firebase UID."))
        }
        if (cleanTargetUid == currentUid) {
            return Result.failure(SocialException("Cannot follow yourself."))
        }

        return runCatching {
            val now = System.currentTimeMillis()
            val targetProfile = remoteDataSource.getPublicProfile(cleanTargetUid)
                ?: throw SocialException("User not found.")
            val following = FollowingDto(
                targetUid = targetProfile.uid,
                displayNameSnapshot = targetProfile.displayName.ifBlank { "Gardener" },
                photoUrlSnapshot = targetProfile.photoUrl,
                followedAt = now,
                gardenLevelSnapshot = targetProfile.gardenLevel,
                gardenLevelTitleSnapshot = targetProfile.gardenLevelTitle,
                weeklyCompletionRateSnapshot = targetProfile.weeklyCompletionRate,
                fullyGrownPlantsSnapshot = targetProfile.fullyGrownPlants,
                highestPlantTypeIdSnapshot = targetProfile.highestPlantTypeId,
                highestPlantGrowthStageSnapshot = targetProfile.highestPlantGrowthStage,
                currentStreakSnapshot = targetProfile.currentStreak
            )
            remoteDataSource.followUser(currentUid = currentUid, following = following)
            cachedFollowingProfileDao.insertProfile(
                CachedFollowingProfileEntity.fromDto(following, now)
            )
            socialCacheMetadataDao.upsert(SocialCacheMetadataEntity(FOLLOWING_CACHE_KEY, now))
            following
        }
    }

    suspend fun unfollow(targetUid: String): Result<Unit> {
        val currentUid = authRepository.currentUser()?.uid
            ?: return Result.failure(SocialException("Sign in with Google to manage friends."))
        return runCatching {
            remoteDataSource.unfollowUser(currentUid, targetUid)
            cachedFollowingProfileDao.deleteByTargetUid(targetUid)
        }
    }

    suspend fun sendNudge(
        targetUid: String,
        messageType: String = NudgeMessageTypes.GARDEN_NUDGE,
        message: String = "Keep your garden growing."
    ): Result<Unit> {
        val authUser = authRepository.currentUser()
            ?: return Result.failure(SocialException("Sign in with Google to send nudges."))
        if (targetUid.isBlank()) {
            return Result.failure(SocialException("Choose a friend to nudge."))
        }
        if (messageType !in NudgeMessageTypes.allowedTypes) {
            return Result.failure(SocialException("Unsupported nudge type."))
        }

        val localUser = userRepository.getUser().first()
        val senderName = localUser?.name?.takeIf { it.isNotBlank() }
            ?: authUser.displayName
            ?: "Gardener"
        return runCatching {
            remoteDataSource.sendNudge(
                NudgeDto(
                    fromUid = authUser.uid,
                    toUid = targetUid,
                    fromName = senderName,
                    fromPhotoUrl = localUser?.avatarUrl ?: authUser.photoUrl,
                    messageType = messageType,
                    message = message,
                    createdAt = System.currentTimeMillis(),
                    readAt = null
                )
            )
        }
    }

    suspend fun loadUnreadNudgesForAppOpen(): Result<List<NudgeDto>> {
        val authUser = authRepository.currentUser()
            ?: return Result.success(emptyList())
        return runCatching {
            val hasCachedFollowing = cachedFollowingProfileDao.observeProfiles()
                .first()
                .isNotEmpty()
            val hasRemoteFollowing = hasCachedFollowing ||
                remoteDataSource.getFollowing(authUser.uid).isNotEmpty()
            if (!hasRemoteFollowing) {
                emptyList()
            } else {
                remoteDataSource.getUnreadNudges(authUser.uid)
            }
        }
    }

    suspend fun markNudgesRead(nudges: List<NudgeDto>): Result<Unit> {
        if (nudges.isEmpty()) return Result.success(Unit)
        return runCatching {
            val readAt = System.currentTimeMillis()
            nudges.forEach { nudge ->
                remoteDataSource.markNudgeRead(nudge.id, readAt)
            }
        }
    }

    private companion object {
        const val LEADERBOARD_CACHE_KEY = "leaderboard"
        const val FOLLOWING_CACHE_KEY = "following"
    }
}

class SocialException(message: String) : Exception(message)
