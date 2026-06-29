package com.habitseed.app.data.repository

import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.dao.UserSettingsDao
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userSettingsDao: UserSettingsDao
) : UserRepository {

    override fun getUser(): Flow<UserEntity?> {
        return userDao.getUser()
    }

    override fun getSettings(): Flow<UserSettingsEntity?> {
        return userSettingsDao.getSettings()
    }

    override suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    override suspend fun addWaterDrops(amount: Int) {
        userDao.addWaterDrops("local_user", amount, System.currentTimeMillis())
    }

    override suspend fun markOnboardingComplete() {
        userDao.markOnboardingComplete(updatedAt = System.currentTimeMillis())
    }

    override suspend fun updateSettings(settings: UserSettingsEntity) {
        userSettingsDao.insertSettings(settings)
    }
}
