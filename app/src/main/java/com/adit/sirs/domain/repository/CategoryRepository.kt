package com.adit.sirs.domain.repository

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.common.Result
import com.adit.sirs.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    // FLOW: Memuat kategori dari Firestore untuk form user atau halaman manajemen admin.
    fun observeActiveCategories(): Flow<List<Category>>
    // FLOW: Memuat kategori dari Firestore untuk form user atau halaman manajemen admin.
    fun observeAllCategories(): Flow<List<Category>>
    // FLOW: Admin membuat kategori laporan baru agar user dapat memilihnya di form.
    suspend fun createCategory(name: String, slug: String, description: String?, mitigationTips: List<String>, recommendedEvidence: List<String>, createdBy: String): Result<String>
    // FLOW: Admin memperbarui nama/deskripsi/panduan kategori yang sudah ada.
    suspend fun updateCategory(categoryId: String, name: String, slug: String, description: String?, mitigationTips: List<String>, recommendedEvidence: List<String>): Result<Unit>
    // FLOW: Admin mengaktifkan atau menonaktifkan kategori tanpa menghapus datanya.
    suspend fun toggleCategoryActive(categoryId: String, isActive: Boolean): Result<Unit>
    // FLOW: Admin menghapus kategori dan mencatat aktivitas pengelolaan kategori.
    suspend fun deleteCategory(categoryId: String): Result<Unit>
}
