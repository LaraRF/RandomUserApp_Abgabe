package com.srh.randomuserapp.data.repository

import com.srh.randomuserapp.data.api.RandomUserApiService
import com.srh.randomuserapp.data.database.UserDao
import com.srh.randomuserapp.data.models.User
import com.srh.randomuserapp.data.models.createManualUser
import com.srh.randomuserapp.data.models.toUser
import com.srh.randomuserapp.utils.QrCodeGenerator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for user data management.
 * Provides abstraction layer between data sources and business logic.
 */
interface UserRepository {

    // Data retrieval methods
    fun getAllUsers(): Flow<List<User>>
    fun getAllUsersSortedByName(): Flow<List<User>>
    fun getAllUsersSortedByDate(): Flow<List<User>>
    fun getAllUsersSortedByDateOfBirth(): Flow<List<User>>
    fun getAllUsersSortedByBirthdayInYear(): Flow<List<User>>
    fun getAllUsersSortedByAge(): Flow<List<User>>
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByQrCode(qrCode: String): User?
    fun getUsersByCreationType(isManual: Boolean): Flow<List<User>>
    fun searchUsersByName(searchQuery: String): Flow<List<User>>

    // Statistics methods
    suspend fun getUserCount(): Int
    suspend fun getManualUserCount(): Int
    suspend fun getApiUserCount(): Int

    // Data modification methods
    suspend fun insertUser(user: User)
    suspend fun insertUsers(users: List<User>)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
    suspend fun deleteUserById(userId: String)
    suspend fun deleteAllUsers()
    suspend fun deleteAllManualUsers()
    suspend fun deleteAllApiUsers()

    // API operations
    suspend fun fetchRandomUsers(count: Int = 10): Result<List<User>>
    suspend fun fetchRandomUser(): Result<User>

    // Manual user creation
    suspend fun createManualUser(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        dateOfBirth: String,
        profilePictureUrl: String = "",
        gender: String = "",
        country: String = "",
        city: String = "",
        street: String = ""
    ): Result<User>
}

/**
 * Implementation of UserRepository interface.
 * Manages data flow between local database and remote API.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val apiService: RandomUserApiService,
    private val qrCodeGenerator: QrCodeGenerator
) : UserRepository {

    // Data retrieval methods
    override fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    override fun getAllUsersSortedByName(): Flow<List<User>> = userDao.getAllUsersSortedByName()

    override fun getAllUsersSortedByDate(): Flow<List<User>> = userDao.getAllUsersSortedByDate()

    override fun getAllUsersSortedByDateOfBirth(): Flow<List<User>> =
        userDao.getAllUsersSortedByDateOfBirth()

    override fun getAllUsersSortedByBirthdayInYear(): Flow<List<User>> =
        userDao.getAllUsersSortedByBirthdayInYear()

    override fun getAllUsersSortedByAge(): Flow<List<User>> =
        userDao.getAllUsersSortedByAge()

    override suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)

    override suspend fun getUserByQrCode(qrCode: String): User? = userDao.getUserByQrCode(qrCode)

    override fun getUsersByCreationType(isManual: Boolean): Flow<List<User>> =
        userDao.getUsersByCreationType(isManual)

    override fun searchUsersByName(searchQuery: String): Flow<List<User>> =
        userDao.searchUsersByName(searchQuery)

    // Statistics methods
    override suspend fun getUserCount(): Int = userDao.getUserCount()

    override suspend fun getManualUserCount(): Int = userDao.getManualUserCount()

    override suspend fun getApiUserCount(): Int = userDao.getApiUserCount()

    // Data modification methods
    override suspend fun insertUser(user: User) = userDao.insertUser(user)

    override suspend fun insertUsers(users: List<User>) = userDao.insertUsers(users)

    override suspend fun updateUser(user: User) = userDao.updateUser(user)

    override suspend fun deleteUser(user: User) = userDao.deleteUser(user)

    override suspend fun deleteUserById(userId: String) = userDao.deleteUserById(userId)

    override suspend fun deleteAllUsers() = userDao.deleteAllUsers()

    override suspend fun deleteAllManualUsers() = userDao.deleteAllManualUsers()

    override suspend fun deleteAllApiUsers() = userDao.deleteAllApiUsers()

    // API operations
    override suspend fun fetchRandomUsers(count: Int): Result<List<User>> {
        return try {
            val response = apiService.getRandomUsers(results = count)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    val users = apiResponse.results.map { userDto ->
                        val qrCodeData = qrCodeGenerator.generateQrCodeData(userDto.login.uuid)
                        userDto.toUser(qrCodeData)
                    }

                    // Save to local database
                    userDao.insertUsers(users)

                    Result.success(users)
                } else {
                    Result.failure(Exception("Empty response from API"))
                }
            } else {
                Result.failure(Exception("API request failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchRandomUser(): Result<User> {
        return try {
            val response = apiService.getRandomUser()

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.results.isNotEmpty()) {
                    val userDto = apiResponse.results.first()
                    val qrCodeData = qrCodeGenerator.generateQrCodeData(userDto.login.uuid)
                    val user = userDto.toUser(qrCodeData)

                    // Save to local database
                    userDao.insertUser(user)

                    Result.success(user)
                } else {
                    Result.failure(Exception("Empty response from API"))
                }
            } else {
                Result.failure(Exception("API request failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Manual user creation
    override suspend fun createManualUser(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        dateOfBirth: String,
        profilePictureUrl: String,
        gender: String,
        country: String,
        city: String,
        street: String
    ): Result<User> {
        return try {
            // Generate unique ID and QR code
            val userId = java.util.UUID.randomUUID().toString()
            val qrCodeData = qrCodeGenerator.generateQrCodeData(userId)

            val user = createManualUser(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                dateOfBirth = dateOfBirth,
                profilePictureUrl = profilePictureUrl,
                gender = gender,
                country = country,
                city = city,
                street = street,
                qrCode = qrCodeData
            )

            userDao.insertUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}