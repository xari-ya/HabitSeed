package com.habitseed.app.di

import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.auth.FirebaseAuthRepository
import com.habitseed.app.data.backup.BackupLocalDataSource
import com.habitseed.app.data.backup.BackupRepository
import com.habitseed.app.data.backup.BackupRemoteDataSource
import com.habitseed.app.data.backup.FirestoreBackupRepository
import com.habitseed.app.data.backup.FirestoreBackupRemoteDataSource
import com.habitseed.app.data.backup.RoomBackupLocalDataSource
import com.habitseed.app.data.local.RoomSessionDataCleaner
import com.habitseed.app.data.local.SessionDataCleaner
import com.habitseed.app.data.social.FirestoreSocialRemoteDataSource
import com.habitseed.app.data.social.SocialRemoteDataSource
import com.habitseed.app.data.repository.HabitRepositoryImpl
import com.habitseed.app.data.repository.ShopRepositoryImpl
import com.habitseed.app.data.repository.SocialRepositoryImpl
import com.habitseed.app.data.repository.UserRepositoryImpl
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.ShopRepository
import com.habitseed.app.domain.repository.SocialRepository
import com.habitseed.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        habitRepositoryImpl: HabitRepositoryImpl
    ): HabitRepository

    @Binds
    @Singleton
    abstract fun bindShopRepository(
        shopRepositoryImpl: ShopRepositoryImpl
    ): ShopRepository

    @Binds
    @Singleton
    abstract fun bindSocialRepository(
        socialRepositoryImpl: SocialRepositoryImpl
    ): SocialRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        firestoreBackupRepository: FirestoreBackupRepository
    ): BackupRepository

    @Binds
    @Singleton
    abstract fun bindBackupLocalDataSource(
        roomBackupLocalDataSource: RoomBackupLocalDataSource
    ): BackupLocalDataSource

    @Binds
    @Singleton
    abstract fun bindBackupRemoteDataSource(
        firestoreBackupRemoteDataSource: FirestoreBackupRemoteDataSource
    ): BackupRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindSessionDataCleaner(
        roomSessionDataCleaner: RoomSessionDataCleaner
    ): SessionDataCleaner

    @Binds
    @Singleton
    abstract fun bindSocialRemoteDataSource(
        firestoreSocialRemoteDataSource: FirestoreSocialRemoteDataSource
    ): SocialRemoteDataSource
}
