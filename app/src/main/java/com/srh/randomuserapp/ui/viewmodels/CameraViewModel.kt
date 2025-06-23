package com.srh.randomuserapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.srh.randomuserapp.data.models.User
import com.srh.randomuserapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for Camera functionality with QR code scanning support
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    /**
     * Get user by QR code for AR scanning
     * @param qrCode QR code data string
     * @return User if found, null otherwise
     */
    suspend fun getUserByQrCode(qrCode: String): User? {
        return try {
            userRepository.getUserByQrCode(qrCode)
        } catch (e: Exception) {
            null
        }
    }
}