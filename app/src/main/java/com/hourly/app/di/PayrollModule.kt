package com.hourly.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hourly.app.feature.payroll.data.DefaultPayrollRepository
import com.hourly.app.feature.payroll.data.InMemoryPayrollApi
import com.hourly.app.feature.payroll.data.PayrollApi
import com.hourly.app.feature.payroll.data.local.PayrollDao
import com.hourly.app.feature.payroll.data.local.PayrollDatabase
import com.hourly.app.feature.payroll.domain.PayrollRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PayrollBindings {
    @Binds
    abstract fun bindPayrollApi(implementation: InMemoryPayrollApi): PayrollApi

    @Binds
    abstract fun bindPayrollRepository(implementation: DefaultPayrollRepository): PayrollRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PayrollStorageModule {
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PayrollDatabase =
        Room.databaseBuilder(context, PayrollDatabase::class.java, "payroll.db")
            .addMigrations(PayrollDatabase.MIGRATION_1_2, PayrollDatabase.MIGRATION_2_3).build()

    @Provides
    fun providePayrollDao(database: PayrollDatabase): PayrollDao = database.payrollDao()
}

/** Hilt entry point for WorkManager, which instantiates this worker itself. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface PayrollSyncEntryPoint {
    fun payrollRepository(): PayrollRepository
}
