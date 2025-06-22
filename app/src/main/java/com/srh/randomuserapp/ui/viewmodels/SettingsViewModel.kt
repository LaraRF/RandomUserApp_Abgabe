package com.srh.randomuserapp.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.srh.randomuserapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for settings screen.
 * Manages app preferences, database operations, and user statistics.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "RandomUserAppPrefs"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Private mutable state
    private val _isLoading = MutableLiveData<Boolean>()
    private val _message = MutableLiveData<String?>()
    private val _userStats = MutableLiveData<AppStats>()

    // Public exposed state
    val isLoading: LiveData<Boolean> = _isLoading
    val message: LiveData<String?> = _message
    val userStats: LiveData<AppStats> = _userStats

    /**
     * Data class for app statistics
     */
    data class AppStats(
        val totalUsers: Int = 0,
        val apiUsers: Int = 0,
        val manualUsers: Int = 0,
        val databaseSizeMB: String = "0.0"
    )

    /**
     * Load current app settings
     */
    fun loadSettings() {
        // Settings are loaded from SharedPreferences when needed
        // This method can be used to trigger any initialization
    }

    /**
     * Load user statistics
     */
    fun loadUserStats() {
        viewModelScope.launch {
            try {
                val totalUsers = userRepository.getUserCount()
                val apiUsers = userRepository.getApiUserCount()
                val manualUsers = userRepository.getManualUserCount()
                val databaseSize = getDatabaseSize()

                _userStats.value = AppStats(
                    totalUsers = totalUsers,
                    apiUsers = apiUsers,
                    manualUsers = manualUsers,
                    databaseSizeMB = databaseSize
                )
            } catch (e: Exception) {
                _message.value = "Failed to load statistics: ${e.message}"
            }
        }
    }

    /**
     * Fill database with random users
     * @param count Number of users to add
     */
    fun fillDatabaseWithRandomUsers(count: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = userRepository.fetchRandomUsers(count)
                result.onSuccess { users ->
                    _message.value = "Successfully added ${users.size} users to database"
                    loadUserStats() // Refresh stats
                }.onFailure { exception ->
                    _message.value = "Failed to fetch users: ${exception.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear all users from database
     */
    fun clearAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.deleteAllUsers()
                _message.value = "All users deleted successfully"
                loadUserStats() // Refresh stats
            } catch (e: Exception) {
                _message.value = "Failed to delete users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear only API users
     */
    fun clearApiUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.deleteAllApiUsers()
                _message.value = "API users deleted successfully"
                loadUserStats() // Refresh stats
            } catch (e: Exception) {
                _message.value = "Failed to delete API users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear only manual users
     */
    fun clearManualUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.deleteAllManualUsers()
                _message.value = "Manual users deleted successfully"
                loadUserStats() // Refresh stats
            } catch (e: Exception) {
                _message.value = "Failed to delete manual users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Export user data (simplified implementation)
     */
    fun exportUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Implement actual data export functionality
                // For now, just show a success message
                _message.value = "Export functionality coming soon"
            } catch (e: Exception) {
                _message.value = "Export failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get/Set dark mode preference
     */
    fun isDarkModeEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    /**
     * Get/Set language preference
     */
    fun getCurrentLanguage(): String {
        return sharedPrefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun setLanguage(languageCode: String) {
        sharedPrefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    /**
     * Get/Set first launch flag
     */
    fun isFirstLaunch(): Boolean {
        return sharedPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    /**
     * Get/Set notifications preference
     */
    fun areNotificationsEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    /**
     * Reset all settings to defaults
     */
    fun resetAllSettings() {
        sharedPrefs.edit().clear().apply()
        _message.value = "Settings reset to defaults"
    }

    /**
     * Calculate database size
     */
    private fun getDatabaseSize(): String {
        return try {
            val dbFile = File(context.getDatabasePath("random_user_database").absolutePath)
            if (dbFile.exists()) {
                val sizeInBytes = dbFile.length()
                val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
                String.format("%.2f", sizeInMB)
            } else {
                "0.0"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    /**
     * Clear current message
     */
    fun clearMessage() {
        _message.value = null
    }
}