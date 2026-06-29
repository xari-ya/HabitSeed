package com.habitseed.app.di

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
}
