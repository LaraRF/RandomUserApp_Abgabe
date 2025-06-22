package com.srh.randomuserapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.asLiveData
import com.srh.randomuserapp.data.models.User
import com.srh.randomuserapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the user list screen.
 * Manages user data, sorting, and loading states.
 */
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    /**
     * Enum for different sort orders
     */
    enum class SortOrder {
        NAME, DATE_CREATED, EMAIL, COUNTRY, DATE_OF_BIRTH
    }

    /**
     * Enum for filter types
     */
    enum class FilterType {
        ALL, API_ONLY, MANUAL_ONLY
    }

    // Private mutable state
    private val _sortOrder = MutableLiveData<SortOrder>(SortOrder.DATE_CREATED)
    private val _isLoading = MutableLiveData<Boolean>(false)
    private val _errorMessage = MutableLiveData<String?>()
    private val _searchQuery = MutableLiveData<String>("")
    private val _filterType = MutableLiveData<FilterType>(FilterType.ALL)

    // Public exposed state
    val sortOrder: LiveData<SortOrder> = _sortOrder
    val isLoading: LiveData<Boolean> = _isLoading
    val errorMessage: LiveData<String?> = _errorMessage
    val searchQuery: LiveData<String> = _searchQuery
    val filterType: LiveData<FilterType> = _filterType

    /**
     * Users based on sort order and search query
     */
    val users: LiveData<List<User>> = _sortOrder.switchMap { sortOrder ->
        _searchQuery.switchMap { searchQuery ->
            _filterType.switchMap { filterType ->
                when {
                    !searchQuery.isNullOrBlank() -> {
                        userRepository.searchUsersByName(searchQuery).asLiveData()
                    }
                    filterType == FilterType.API_ONLY -> {
                        userRepository.getUsersByCreationType(false).asLiveData()
                    }
                    filterType == FilterType.MANUAL_ONLY -> {
                        userRepository.getUsersByCreationType(true).asLiveData()
                    }
                    sortOrder == SortOrder.NAME -> {
                        userRepository.getAllUsersSortedByName().asLiveData()
                    }
                    sortOrder == SortOrder.DATE_OF_BIRTH -> {
                        userRepository.getAllUsersSortedByDateOfBirth().asLiveData()
                    }
                    else -> {
                        userRepository.getAllUsersSortedByDate().asLiveData()
                    }
                }
            }
        }
    }

    /**
     * Statistics for user counts
     */
    val userStats: LiveData<UserStats> = users.switchMap { userList ->
        val stats = UserStats(
            totalUsers = userList.size,
            apiUsers = userList.count { !it.isManuallyCreated },
            manualUsers = userList.count { it.isManuallyCreated },
            countries = userList.map { it.country }.distinct().sorted(),
            genders = userList.map { it.gender }.distinct().sorted()
        )
        MutableLiveData(stats)
    }

    init {
        // Load initial data if database is empty
        checkAndLoadInitialData()
    }

    /**
     * Sets the sort order for user list
     * @param newSortOrder New sort order to apply
     */
    fun setSortOrder(newSortOrder: SortOrder) {
        _sortOrder.value = newSortOrder
    }

    /**
     * Sets search query for filtering users
     * @param query Search query string
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }

    /**
     * Sets filter type
     * @param filterType Filter type to apply
     */
    fun setFilterType(filterType: FilterType) {
        _filterType.value = filterType
    }

    /**
     * Clears the current search query and filters
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _filterType.value = FilterType.ALL
    }

    /**
     * Clears only filters, keeps search query
     */
    fun clearFilters() {
        _filterType.value = FilterType.ALL
    }

    /**
     * Fetches random users from API
     * @param count Number of users to fetch (default: 10)
     */
    fun fetchRandomUsers(count: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            userRepository.fetchRandomUsers(count)
                .onSuccess {
                    _errorMessage.value = null
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to fetch users"
                }

            _isLoading.value = false
        }
    }

    /**
     * Refreshes user data from API
     */
    fun refreshUsers() {
        fetchRandomUsers()
    }

    /**
     * Deletes all users from database
     */
    fun deleteAllUsers() {
        viewModelScope.launch {
            try {
                userRepository.deleteAllUsers()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete users: ${e.message}"
            }
        }
    }

    /**
     * Deletes only API-fetched users
     */
    fun deleteApiUsers() {
        viewModelScope.launch {
            try {
                userRepository.deleteAllApiUsers()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete API users: ${e.message}"
            }
        }
    }

    /**
     * Deletes only manually created users
     */
    fun deleteManualUsers() {
        viewModelScope.launch {
            try {
                userRepository.deleteAllManualUsers()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete manual users: ${e.message}"
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
     * Checks if database is empty and loads initial data if needed
     */
    private fun checkAndLoadInitialData() {
        viewModelScope.launch {
            val userCount = userRepository.getUserCount()
            if (userCount == 0) {
                fetchRandomUsers(10) // Load 10 initial users
            }
        }
    }

    /**
     * Data class for user statistics
     */
    data class UserStats(
        val totalUsers: Int = 0,
        val apiUsers: Int = 0,
        val manualUsers: Int = 0,
        val countries: List<String> = emptyList(),
        val genders: List<String> = emptyList()
    )
}