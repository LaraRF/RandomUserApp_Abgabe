package com.srh.randomuserapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.srh.randomuserapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for adding manual users.
 */
@HiltViewModel
class AddUserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // Private mutable state
    private val _isLoading = MutableLiveData<Boolean>(false)
    private val _message = MutableLiveData<String?>()
    private val _userCreated = MutableLiveData<Boolean>(false)

    // Public exposed state
    val isLoading: LiveData<Boolean> = _isLoading
    val message: LiveData<String?> = _message
    val userCreated: LiveData<Boolean> = _userCreated

    /**
     * Creates a new manual user
     */
    fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        dateOfBirth: String,
        gender: String = "",
        country: String = "",
        city: String = "",
        street: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = userRepository.createManualUser(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = phone,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    country = country,
                    city = city,
                    street = street
                )

                result.onSuccess {
                    _message.value = "User created successfully"
                    _userCreated.value = true
                }.onFailure { exception ->
                    _message.value = "Failed to create user: ${exception.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear current message
     */
    fun clearMessage() {
        _message.value = null
    }
}