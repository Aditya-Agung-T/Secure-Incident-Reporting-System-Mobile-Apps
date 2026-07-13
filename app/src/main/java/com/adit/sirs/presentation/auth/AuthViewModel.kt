package com.adit.sirs.presentation.auth

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adit.sirs.core.common.UiState
import com.adit.sirs.domain.model.User
import com.adit.sirs.domain.repository.AuthRepository
import com.adit.sirs.core.common.Result
import com.adit.sirs.core.firebase.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// FLOW: Annotation ini membuat ViewModel dapat menerima dependency dari Hilt secara otomatis.
@HiltViewModel
// FLOW: `AuthViewModel` menyimpan state layar dan menjadi penghubung antara UI Compose dengan repository.
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    // FLOW: `private val _loginState` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    // FLOW: `val loginState: StateFlow<UiState<User>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    // FLOW: `private val _registerState` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Idle)
    // FLOW: `val registerState: StateFlow<UiState<User>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()

    // FLOW: `private val _resetPasswordState` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _resetPasswordState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    // FLOW: `val resetPasswordState: StateFlow<UiState<Unit>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val resetPasswordState: StateFlow<UiState<Unit>> = _resetPasswordState.asStateFlow()

    // FLOW: `private val _currentUser` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _currentUser = MutableStateFlow<User?>(null)
    // FLOW: `val currentUser: StateFlow<User?>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkCurrentUser()
    }

    // FLOW: Mengecek user aktif sebagai titik awal penentuan dashboard dan hak akses.
    private fun checkCurrentUser() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                when (val result = authRepository.getCurrentUser()) {
                    is Result.Success -> _currentUser.value = result.data
                    else -> _currentUser.value = null
                }
            }
        }
    }

    // FLOW: Memproses login user, mengubah state UI, lalu mengarahkan user sesuai role.
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _loginState.value = UiState.Success(result.data)
                    analyticsHelper.logLoginSuccess(result.data.role.value)
                }
                is Result.Error -> {
                    _loginState.value = UiState.Error(result.message ?: "Login failed")
                }
                is Result.Loading -> { }
            }
        }
    }

    // FLOW: Memproses pendaftaran akun user baru dan menyiapkan profil awal di Firestore.
    fun register(name: String, email: String, password: String) {
        val passwordValidationError = validatePassword(password)
        if (passwordValidationError != null) {
            _registerState.value = UiState.Error(passwordValidationError)
            return
        }

        viewModelScope.launch {
            _registerState.value = UiState.Loading
            when (val result = authRepository.register(name, email, password)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _registerState.value = UiState.Success(result.data)
                    analyticsHelper.logLoginSuccess(result.data.role.value)
                }
                is Result.Error -> {
                    _registerState.value = UiState.Error(result.message ?: "Pendaftaran gagal")
                }
                is Result.Loading -> { }
            }
        }
    }

    // FLOW: Memvalidasi kekuatan password sebelum registrasi agar akun memenuhi standar keamanan aplikasi.
    private fun validatePassword(password: String): String? {
        return when {
            password.length < 12 -> "Password minimal 12 karakter"
            !password.any { it.isUpperCase() } -> "Password harus memiliki huruf besar"
            !password.any { it.isLowerCase() } -> "Password harus memiliki huruf kecil"
            !password.any { it.isDigit() } -> "Password harus memiliki angka"
            !password.any { !it.isLetterOrDigit() } -> "Password harus memiliki karakter khusus"
            else -> null
        }
    }

    // FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = UiState.Loading
            when (val result = authRepository.sendPasswordReset(email)) {
                is Result.Success -> {
                    _resetPasswordState.value = UiState.Success(Unit)
                }
                is Result.Error -> {
                    _resetPasswordState.value = UiState.Error(result.message ?: "Failed to send reset email")
                }
                is Result.Loading -> { }
            }
        }
    }

    // FLOW: Menghapus sesi/token lokal lalu mengembalikan aplikasi ke halaman login.
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _loginState.value = UiState.Idle
            _registerState.value = UiState.Idle
        }
    }

    // FLOW: Memproses login user, mengubah state UI, lalu mengarahkan user sesuai role.
    fun resetLoginState() {
        _loginState.value = UiState.Idle
    }

    // FLOW: Memproses pendaftaran akun user baru dan menyiapkan profil awal di Firestore.
    fun resetRegisterState() {
        _registerState.value = UiState.Idle
    }

    // FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
    fun resetPasswordResetState() {
        _resetPasswordState.value = UiState.Idle
    }
}
