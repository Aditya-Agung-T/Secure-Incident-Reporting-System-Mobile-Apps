package com.adit.sirs.presentation.profile

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adit.sirs.core.common.Result
import com.adit.sirs.core.common.UiState
import com.adit.sirs.domain.model.User
import com.adit.sirs.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// FLOW: Annotation ini membuat ViewModel dapat menerima dependency dari Hilt secara otomatis.
@HiltViewModel
// FLOW: `ProfileViewModel` menyimpan state layar dan menjadi penghubung antara UI Compose dengan repository.
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // FLOW: `private val _user` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _user = MutableStateFlow<UiState<User>>(UiState.Loading)
    // FLOW: `val user: StateFlow<UiState<User>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val user: StateFlow<UiState<User>> = _user.asStateFlow()

    init {
        loadProfile()
    }

    // FLOW: Fungsi `loadProfile` menjalankan langkah khusus pada file ini dan menjaga alur menampilkan dan memuat data profil pengguna.
    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    result.data?.let { _user.value = UiState.Success(it) }
                        ?: run { _user.value = UiState.Error("User tidak ditemukan") }
                }
                is Result.Error -> _user.value = UiState.Error(result.message ?: "Failed")
                is Result.Loading -> {}
            }
        }
    }
}
