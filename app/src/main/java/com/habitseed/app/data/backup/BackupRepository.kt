package com.habitseed.app.data.backup

import androidx.room.withTransaction
import com.google.firebase.firestore.FirebaseFirestore
import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.local.AppDatabase
import com.habitseed.app.data.local.dao.HabitDao
import com.habitseed.app.data.local.dao.HabitLogDao
import com.habitseed.app.data.local.dao.PurchaseDao
import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.dao.UserSettingsDao
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.local.entity.PurchaseEntity
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.data.local.entity.UserUnlockedPlantEntity
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

interface BackupRepository {
    suspend fun backupData(): Result<Unit>
    suspend fun restoreDataIfNeeded(): RestoreResult
}

sealed interface RestoreResult {
    val message: String

    data class Restored(val backedUpAt: Long) : RestoreResult {
        override val message: String = "Backup restored."
    }

    data object NoBackupFound : RestoreResult {
        override val message: String = "No backup found."
    }

    data object SkippedLocalDataPresent : RestoreResult {
        override val message: String = "Local data already exists. Restore skipped."
    }

    data class Failed(
        override val message: String,
        val cause: Throwable? = null
    ) : RestoreResult
}

interface BackupRemoteDataSource {
    suspend fun saveBackup(uid: String, backup: FullBackupDto)
    suspend fun loadBackup(uid: String): FullBackupDto?
}

class FirestoreBackupRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : BackupRemoteDataSource {
    override suspend fun saveBackup(uid: String, backup: FullBackupDto) {
        firestore.collection(USER_BACKUPS)
            .document(uid)
            .set(backup)
            .await()
    }

    override suspend fun loadBackup(uid: String): FullBackupDto? {
        val doc = firestore.collection(USER_BACKUPS)
            .document(uid)
            .get()
            .await()

        if (!doc.exists()) return null
        return doc.toObject(FullBackupDto::class.java)
    }

    private companion object {
        const val USER_BACKUPS = "user_backups"
    }
}

interface BackupLocalDataSource {
    suspend fun createBackup(now: Long): FullBackupDto?
    suspend fun hasLocalHabitData(): Boolean
    suspend fun restoreBackup(backup: FullBackupDto)
}

class RoomBackupLocalDataSource @Inject constructor(
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val userSettingsDao: UserSettingsDao,
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val purchaseDao: PurchaseDao
) : BackupLocalDataSource {
    override suspend fun createBackup(now: Long): FullBackupDto? {
        val user = userDao.getUserById() ?: return null
        val settings = userSettingsDao.getSettingsSync() ?: UserSettingsEntity()
        val habits = habitDao.getAllHabitsSync()
        val logs = habitLogDao.getAllLogsSync()
        val purchases = purchaseDao.getAllPurchases()
        val unlockedPlants = purchaseDao.getAllUnlockedPlants()

        return FullBackupDto(
            user = user.toBackupDto(),
            settings = settings.toBackupDto(),
            habits = habits.map { it.toBackupDto() },
            habitLogs = logs.map { it.toBackupDto() },
            purchases = purchases.map { it.toBackupDto() },
            unlockedPlants = unlockedPlants.map { it.toBackupDto() },
            backedUpAt = now
        )
    }

    override suspend fun hasLocalHabitData(): Boolean {
        return habitDao.getAllHabitsSync().isNotEmpty()
    }

    override suspend fun restoreBackup(backup: FullBackupDto) {
        db.withTransaction {
            val existingUser = userDao.getUserById()
            val updatedUser = existingUser?.copy(
                name = backup.user.name.ifBlank { existingUser.name },
                waterDrops = backup.user.waterDrops,
                gardenXp = backup.user.gardenXp,
                lifetimeDropsEarned = backup.user.lifetimeDropsEarned,
                lastPerfectDayBonusDateKey = backup.user.lastPerfectDayBonusDateKey,
                currentStreak = backup.user.currentStreak,
                bestStreak = backup.user.bestStreak,
                selectedTheme = backup.user.selectedTheme,
                onboardingComplete = backup.user.onboardingComplete,
                joinedAt = backup.user.joinedAt.takeIf { it > 0 } ?: existingUser.joinedAt,
                createdAt = backup.user.createdAt.takeIf { it > 0 } ?: existingUser.createdAt
            ) ?: UserEntity(
                name = backup.user.name.ifBlank { "Gardener" },
                waterDrops = backup.user.waterDrops,
                gardenXp = backup.user.gardenXp,
                lifetimeDropsEarned = backup.user.lifetimeDropsEarned,
                lastPerfectDayBonusDateKey = backup.user.lastPerfectDayBonusDateKey,
                currentStreak = backup.user.currentStreak,
                bestStreak = backup.user.bestStreak,
                selectedTheme = backup.user.selectedTheme,
                onboardingComplete = backup.user.onboardingComplete,
                joinedAt = backup.user.joinedAt.takeIf { it > 0 } ?: System.currentTimeMillis(),
                createdAt = backup.user.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
            )
            userDao.insertUser(updatedUser)

            userSettingsDao.insertSettings(
                UserSettingsEntity(
                    notificationsEnabled = backup.settings.notificationsEnabled,
                    reminderHour = backup.settings.reminderHour,
                    reminderMinute = backup.settings.reminderMinute,
                    darkModeEnabled = backup.settings.darkModeEnabled,
                    soundEnabled = backup.settings.soundEnabled,
                    hapticsEnabled = backup.settings.hapticsEnabled
                )
            )

            val habitIdMap = mutableMapOf<Long, Long>()
            backup.habits.forEach { habitDto ->
                val newHabit = HabitEntity(
                    name = habitDto.name,
                    description = habitDto.description,
                    iconName = habitDto.iconName,
                    colorHex = habitDto.colorHex,
                    frequencyType = habitDto.frequencyType,
                    weeklyDaysMask = habitDto.weeklyDaysMask,
                    targetCount = habitDto.targetCount,
                    plantTypeId = habitDto.plantTypeId,
                    currentStreak = habitDto.currentStreak,
                    bestStreak = habitDto.bestStreak,
                    totalCompletions = habitDto.totalCompletions,
                    reminderEnabled = habitDto.reminderEnabled,
                    reminderHour = habitDto.reminderHour,
                    reminderMinute = habitDto.reminderMinute,
                    isArchived = habitDto.isArchived,
                    createdAt = habitDto.createdAt,
                    updatedAt = habitDto.updatedAt,
                    plantGrowthLevel = habitDto.plantGrowthLevel
                )
                val newId = habitDao.insertHabit(newHabit)
                habitIdMap[habitDto.localId] = newId
            }

            backup.habitLogs.forEach { logDto ->
                val newHabitId = habitIdMap[logDto.habitLocalId] ?: return@forEach
                habitLogDao.insertLog(
                    HabitLogEntity(
                        habitId = newHabitId,
                        dateKey = logDto.dateKey,
                        completedAt = logDto.completedAt,
                        status = logDto.status,
                        note = logDto.note,
                        waterDropsAwarded = logDto.waterDropsAwarded
                    )
                )
            }

            backup.purchases.forEach { purchaseDto ->
                purchaseDao.insertPurchase(
                    PurchaseEntity(
                        userId = "local_user",
                        shopItemId = purchaseDto.shopItemId,
                        pricePaidDrops = purchaseDto.pricePaidDrops,
                        purchasedAt = purchaseDto.purchasedAt
                    )
                )
            }

            backup.unlockedPlants.forEach { plantDto ->
                purchaseDao.insertUnlockedPlant(
                    UserUnlockedPlantEntity(
                        userId = "local_user",
                        plantTypeId = plantDto.plantTypeId,
                        unlockedAt = plantDto.unlockedAt
                    )
                )
            }
        }
    }
}

class FirestoreBackupRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val localDataSource: BackupLocalDataSource,
    private val remoteDataSource: BackupRemoteDataSource
) : BackupRepository {
    override suspend fun backupData(): Result<Unit> {
        val currentUser = authRepository.currentUser()
            ?: return Result.failure(Exception("Not signed in"))

        return runCatching {
            val backup = localDataSource.createBackup(System.currentTimeMillis())
                ?: return@runCatching
            remoteDataSource.saveBackup(currentUser.uid, backup)
        }
    }

    override suspend fun restoreDataIfNeeded(): RestoreResult {
        val currentUser = authRepository.currentUser()
            ?: return RestoreResult.Failed("Not signed in")

        return try {
            if (localDataSource.hasLocalHabitData()) {
                return RestoreResult.SkippedLocalDataPresent
            }

            val backup = try {
                remoteDataSource.loadBackup(currentUser.uid)
            } catch (error: Throwable) {
                return RestoreResult.Failed(
                    message = error.message?.let { "Could not read backup. $it" }
                        ?: "Could not read backup.",
                    cause = error
                )
            } ?: return RestoreResult.NoBackupFound

            if (!backup.hasMeaningfulContent()) {
                return RestoreResult.Failed("Backup document is empty or invalid.")
            }

            localDataSource.restoreBackup(backup)
            RestoreResult.Restored(backup.backedUpAt)
        } catch (error: Throwable) {
            RestoreResult.Failed(
                message = error.message?.let { "Could not restore backup. $it" }
                    ?: "Could not restore backup.",
                cause = error
            )
        }
    }

    private fun FullBackupDto.hasMeaningfulContent(): Boolean {
        return backedUpAt > 0 ||
            user.name.isNotBlank() ||
            user.joinedAt > 0 ||
            user.createdAt > 0 ||
            habits.isNotEmpty() ||
            habitLogs.isNotEmpty() ||
            purchases.isNotEmpty() ||
            unlockedPlants.isNotEmpty()
    }
}
