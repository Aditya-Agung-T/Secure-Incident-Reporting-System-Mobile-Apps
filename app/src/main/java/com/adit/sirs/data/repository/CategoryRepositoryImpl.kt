package com.adit.sirs.data.repository

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.common.Result
import com.adit.sirs.core.error.ErrorMapper
import com.adit.sirs.data.mapper.CategoryMapper
import com.adit.sirs.data.remote.FirestoreCategoryDataSource
import com.adit.sirs.domain.model.Category
import com.adit.sirs.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `CategoryRepositoryImpl` berisi implementasi operasi domain dengan memanggil data source yang sesuai.
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDataSource: FirestoreCategoryDataSource
) : CategoryRepository {

    // FLOW: Memuat kategori dari Firestore untuk form user atau halaman manajemen admin.
    override fun observeActiveCategories(): Flow<List<Category>> {
        return categoryDataSource.observeActiveCategories().map { dtos ->
            dtos.map { CategoryMapper.toDomain(it) }
        }
    }

    // FLOW: Memuat kategori dari Firestore untuk form user atau halaman manajemen admin.
    override fun observeAllCategories(): Flow<List<Category>> {
        return categoryDataSource.observeAllCategories().map { dtos ->
            dtos.map { CategoryMapper.toDomain(it) }
        }
    }

    // FLOW: Admin membuat kategori laporan baru agar user dapat memilihnya di form.
    override suspend fun createCategory(name: String, slug: String, description: String?, mitigationTips: List<String>, recommendedEvidence: List<String>, createdBy: String): Result<String> {
        return try {
            val id = categoryDataSource.createCategory(name, slug, description, mitigationTips, recommendedEvidence, createdBy)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Admin memperbarui nama/deskripsi/panduan kategori yang sudah ada.
    override suspend fun updateCategory(categoryId: String, name: String, slug: String, description: String?, mitigationTips: List<String>, recommendedEvidence: List<String>): Result<Unit> {
        return try {
            categoryDataSource.updateCategory(categoryId, name, slug, description, mitigationTips, recommendedEvidence)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Admin mengaktifkan atau menonaktifkan kategori tanpa menghapus datanya.
    override suspend fun toggleCategoryActive(categoryId: String, isActive: Boolean): Result<Unit> {
        return try {
            categoryDataSource.toggleCategoryActive(categoryId, isActive)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Admin menghapus kategori dan mencatat aktivitas pengelolaan kategori.
    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            categoryDataSource.deleteCategory(categoryId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }
}
