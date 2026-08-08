package com.docai.kabelkopf.di

import com.docai.kabelkopf.pro.FakePremiumRepository
import com.docai.kabelkopf.pro.PremiumRepository
import com.docai.kabelkopf.util.CrashReporter
import com.docai.kabelkopf.util.FileCrashReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCrashReporter(impl: FileCrashReporter): CrashReporter

    @Binds
    abstract fun bindPremiumRepository(impl: FakePremiumRepository): PremiumRepository
}