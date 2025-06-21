package com.srh.randomuserapp.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.srh.randomuserapp.data.models.User
import com.srh.randomuserapp.data.repository.UserRepository
import com.srh.randomuserapp.utils.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for user detail screen.
 * Manages single user data, editing, and QR code generation.
 */
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val qrCodeGenerator: QrCodeGenerator
) : ViewModel() {

    // Private mutable state
    private val _user = MutableLiveData<User?>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String?>()
    private val _qrCodeBitmap = MutableLiveData<Bitmap?>()

    // Public exposed state
    val user: LiveData<User?> = _user
    val isLoading: LiveData<Boolean> = _isLoading
    val errorMessage: LiveData<String?> = _errorMessage
    val qrCodeBitmap: LiveData<Bitmap?> = _qrCodeBitmap

    /**
     * Loads user details by ID
     * @param userId User's unique identifier
     */
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    _user.value = user
                } else {
                    _errorMessage.value = "User not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Generates QR code bitmap for the current user
     * @param userId User's unique identifier
     */
    fun generateQrCodeBitmap(userId: String) {
        viewModelScope.launch {
            try {
                val bitmap = qrCodeGenerator.generateQrCodeBitmap(userId)
                _qrCodeBitmap.value = bitmap
            } catch (e: Exception) {
                _errorMessage.value = "Failed to generate QR code: ${e.message}"
            }
        }
    }

    /**
     * Updates the current user
     * @param updatedUser Updated user data
     */
    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            try {
                val userToUpdate = updatedUser.copy(
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.updateUser(userToUpdate)
                _user.value = userToUpdate
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update user: ${e.message}"
            }
        }
    }

    /**
     * Deletes the current user
     */
    fun deleteUser() {
        viewModelScope.launch {
            try {
                _user.value?.let { user ->
                    userRepository.deleteUser(user)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete user: ${e.message}"
            }
        }
    }

    /**
     * Clears the current error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clears the QR code bitmap
     */
    fun clearQrCode() {
        _qrCodeBitmap.value = null
    }
}