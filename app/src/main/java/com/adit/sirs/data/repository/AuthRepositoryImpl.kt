package com.adit.sirs.data.repository

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.common.Result
import com.adit.sirs.core.error.ErrorMapper
import com.adit.sirs.data.mapper.UserMapper
import com.adit.sirs.data.remote.FcmTokenDataSource
import com.adit.sirs.data.remote.FirebaseAuthDataSource
import com.adit.sirs.data.remote.FirestoreActivityLogDataSource
import com.adit.sirs.data.remote.FirestoreUserDataSource
import com.adit.sirs.domain.model.User
import com.adit.sirs.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `AuthRepositoryImpl` berisi implementasi operasi domain dengan memanggil data source yang sesuai.
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val userDataSource: FirestoreUserDataSource,
    private val activityLogDataSource: FirestoreActivityLogDataSource,
    private val fcmTokenDataSource: FcmTokenDataSource
) : AuthRepository {

    // FLOW: Memproses login user, mengubah state UI, lalu mengarahkan user sesuai role.
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.signIn(email, password)
            val userDto = userDataSource.getUserProfile(firebaseUser.uid)
                ?: throw Exception("User profile not found")

            if (!userDto.isActive) {
                authDataSource.signOut()
                return Result.Error(Exception("Account is inactive"), "Your account has been deactivated.")
            }

            userDataSource.updateLastLogin(firebaseUser.uid)

            try {
                fcmTokenDataSource.registerToken(firebaseUser.uid, userDto.role)
            } catch (_: Exception) { }

            Result.Success(UserMapper.toDomain(userDto))
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Memproses pendaftaran akun user baru dan menyiapkan profil awal di Firestore.
    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.register(email, password)
            userDataSource.createUserProfile(firebaseUser.uid, name, email)

            val userDto = userDataSource.getUserProfile(firebaseUser.uid)
                ?: throw Exception("Failed to create user profile")

            try {
                activityLogDataSource.createLog(
                    actorId = firebaseUser.uid,
                    actorName = name,
                    actorRole = "user",
                    action = "user.registered",
                    entityType = "user",
                    entityId = firebaseUser.uid,
                    description = "User $name registered"
                )
            } catch (_: Exception) { }

            try {
                fcmTokenDataSource.registerToken(firebaseUser.uid, "user")
            } catch (_: Exception) { }

            Result.Success(UserMapper.toDomain(userDto))
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Menghapus sesi/token lokal lalu mengembalikan aplikasi ke halaman login.
    override suspend fun logout(): Result<Unit> {
        return try {
            fcmTokenDataSource.removeToken()
            authDataSource.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            authDataSource.signOut()
            Result.Success(Unit)
        }
    }

    // FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            authDataSource.sendPasswordResetEmail(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Fungsi `getCurrentUser` menjalankan langkah khusus pada file ini dan menjaga alur menjembatani domain layer dengan data source Firebase atau Cloudinary.
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val uid = authDataSource.currentUid ?: return Result.Success(null)
            val userDto = userDataSource.getUserProfile(uid)
            Result.Success(userDto?.let { UserMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Fungsi `isLoggedIn` menjalankan langkah khusus pada file ini dan menjaga alur menjembatani domain layer dengan data source Firebase atau Cloudinary.
    override fun isLoggedIn(): Boolean = authDataSource.isLoggedIn()
}
