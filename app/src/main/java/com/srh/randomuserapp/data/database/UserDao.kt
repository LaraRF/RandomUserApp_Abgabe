package com.srh.randomuserapp.data.database

import androidx.room.*
import com.srh.randomuserapp.data.models.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for User entity.
 * Provides database operations for user management.
 */
@Dao
interface UserDao {

    /**
     * Get all users from database, ordered by creation date (newest first)
     * @return Flow of user list for reactive updates
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    /**
     * Get all users sorted by name (firstName, then lastName)
     * @return Flow of user list sorted alphabetically
     */
    @Query("SELECT * FROM users ORDER BY firstName ASC, lastName ASC")
    fun getAllUsersSortedByName(): Flow<List<User>>

    /**
     * Get all users sorted by date of birth
     * @return Flow of user list sorted by date of birth
     */
    @Query("SELECT * FROM users ORDER BY dateOfBirth ASC")
    fun getAllUsersSortedByDateOfBirth(): Flow<List<User>>

    /**
     * Get all users sorted by birthday in year (1.1. to 31.12., ignoring year)
     * @return Flow of user list sorted by birthday within year
     */
    @Query("""
        SELECT * FROM users 
        ORDER BY 
            CASE 
                WHEN substr(dateOfBirth, 1, 3) = 'Jan' THEN '01'
                WHEN substr(dateOfBirth, 1, 3) = 'Feb' THEN '02'
                WHEN substr(dateOfBirth, 1, 3) = 'Mar' THEN '03'
                WHEN substr(dateOfBirth, 1, 3) = 'Apr' THEN '04'
                WHEN substr(dateOfBirth, 1, 3) = 'May' THEN '05'
                WHEN substr(dateOfBirth, 1, 3) = 'Jun' THEN '06'
                WHEN substr(dateOfBirth, 1, 3) = 'Jul' THEN '07'
                WHEN substr(dateOfBirth, 1, 3) = 'Aug' THEN '08'
                WHEN substr(dateOfBirth, 1, 3) = 'Sep' THEN '09'
                WHEN substr(dateOfBirth, 1, 3) = 'Oct' THEN '10'
                WHEN substr(dateOfBirth, 1, 3) = 'Nov' THEN '11'
                WHEN substr(dateOfBirth, 1, 3) = 'Dec' THEN '12'
                ELSE '13'
            END,
            CAST(substr(dateOfBirth, 5, 2) AS INTEGER)
    """)
    fun getAllUsersSortedByBirthdayInYear(): Flow<List<User>>

    /**
     * Get all users sorted by age (youngest first)
     * @return Flow of user list sorted by age
     */
    @Query("""
        SELECT * FROM users 
        ORDER BY 
            CAST(substr(dateOfBirth, -4) AS INTEGER) DESC
    """)
    fun getAllUsersSortedByAge(): Flow<List<User>>

    /**
     * Get all users sorted by creation date (newest first)
     * @return Flow of user list sorted by date
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersSortedByDate(): Flow<List<User>>

    /**
     * Get user by unique ID
     * @param userId User's unique identifier
     * @return User entity or null if not found
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    /**
     * Get user by QR code (for AR functionality)
     * @param qrCode QR code data string
     * @return User entity or null if not found
     */
    @Query("SELECT * FROM users WHERE qrCode = :qrCode LIMIT 1")
    suspend fun getUserByQrCode(qrCode: String): User?

    /**
     * Get users by manual creation status
     * @param isManual True for manually created users, false for API users
     * @return Flow of filtered user list
     */
    @Query("SELECT * FROM users WHERE isManuallyCreated = :isManual ORDER BY createdAt DESC")
    fun getUsersByCreationType(isManual: Boolean): Flow<List<User>>

    /**
     * Search users by name (first name or last name)
     * @param searchQuery Search term
     * @return Flow of matching users
     */
    @Query("""
        SELECT * FROM users 
        WHERE firstName LIKE '%' || :searchQuery || '%' 
        OR lastName LIKE '%' || :searchQuery || '%'
        ORDER BY firstName ASC, lastName ASC
    """)
    fun searchUsersByName(searchQuery: String): Flow<List<User>>

    /**
     * Get total count of users in database
     * @return Number of users
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    /**
     * Get count of manually created users
     * @return Number of manual users
     */
    @Query("SELECT COUNT(*) FROM users WHERE isManuallyCreated = 1")
    suspend fun getManualUserCount(): Int

    /**
     * Get count of API-fetched users
     * @return Number of API users
     */
    @Query("SELECT COUNT(*) FROM users WHERE isManuallyCreated = 0")
    suspend fun getApiUserCount(): Int

    /**
     * Insert a single user into database
     * @param user User entity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * Insert multiple users into database (batch operation)
     * @param users List of user entities to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    /**
     * Update existing user in database
     * @param user Updated user entity
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * Delete a specific user from database
     * @param user User entity to delete
     */
    @Delete
    suspend fun deleteUser(user: User)

    /**
     * Delete user by ID
     * @param userId User's unique identifier
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    /**
     * Delete all users from database (for settings/reset functionality)
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    /**
     * Delete only manually created users
     */
    @Query("DELETE FROM users WHERE isManuallyCreated = 1")
    suspend fun deleteAllManualUsers()

    /**
     * Delete only API-fetched users
     */
    @Query("DELETE FROM users WHERE isManuallyCreated = 0")
    suspend fun deleteAllApiUsers()
}