package com.srh.randomuserapp.di

import com.srh.randomuserapp.data.repository.UserRepository
import com.srh.randomuserapp.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies.
 * Binds repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds UserRepository interface to UserRepositoryImpl.
     * This allows injection of UserRepository while using the concrete implementation.
     *
     * @param userRepositoryImpl Concrete implementation
     * @return UserRepository interface
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}