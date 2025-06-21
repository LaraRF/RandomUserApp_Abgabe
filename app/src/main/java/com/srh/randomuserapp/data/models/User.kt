package com.srh.randomuserapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * User entity for Room database storage.
 * Represents both API-fetched users and manually created users.
 *
 * @param id Unique identifier for the user
 * @param firstName User's first name
 * @param lastName User's last name
 * @param email User's email address
 * @param phone User's phone number
 * @param dateOfBirth User's birth date (ISO format)
 * @param profilePictureUrl URL to user's profile picture
 * @param gender User's gender
 * @param country User's country
 * @param city User's city
 * @param street User's street address
 * @param qrCode Generated QR code data for this user
 * @param isManuallyCreated True if user was created manually, false if from API
 * @param createdAt Timestamp when user was added to database
 * @param updatedAt Timestamp when user was last updated
 */
@Entity(tableName = "users")
@Parcelize
data class User(
    @PrimaryKey
    val id: String,

    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String,
    val profilePictureUrl: String,

    // Additional user information
    val gender: String,
    val country: String,
    val city: String,
    val street: String,

    // QR Code for AR functionality
    val qrCode: String,

    // Metadata
    val isManuallyCreated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {

    /**
     * Returns the full name of the user
     */
    val fullName: String
        get() = "$firstName $lastName"

    /**
     * Returns a formatted address string
     */
    val fullAddress: String
        get() = "$street, $city, $country"
}