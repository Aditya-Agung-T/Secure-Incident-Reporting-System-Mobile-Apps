package com.adit.sirs.data.di

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.data.repository.AdminRepositoryImpl
import com.adit.sirs.data.repository.AuthRepositoryImpl
import com.adit.sirs.data.repository.CategoryRepositoryImpl
import com.adit.sirs.data.repository.ReportRepositoryImpl
import com.adit.sirs.domain.repository.AdminRepository
import com.adit.sirs.domain.repository.AuthRepository
import com.adit.sirs.domain.repository.CategoryRepository
import com.adit.sirs.domain.repository.ReportRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository
}
