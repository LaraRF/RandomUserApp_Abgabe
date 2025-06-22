
package com.srh.randomuserapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * User entity for Room database.
 * Represents both API-fetched and manually created users.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String, // Eindeutige ID - entweder UUID von API oder generierte UUID

    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String,
    val profilePictureUrl: String = "",

    // Additional information
    val gender: String = "",
    val country: String = "",
    val city: String = "",
    val street: String = "",

    // App-specific metadata
    val qrCode: String, // QR code data für AR functionality
    val isManuallyCreated: Boolean = false, // true wenn manuell erstellt
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {

    /**
     * Computed properties for UI display
     */
    val fullName: String
        get() = "$firstName $lastName"

    val fullAddress: String
        get() = when {
            street.isNotBlank() && city.isNotBlank() && country.isNotBlank() ->
                "$street, $city, $country"
            city.isNotBlank() && country.isNotBlank() ->
                "$city, $country"
            country.isNotBlank() ->
                country
            else ->
                "Address not available"
        }

    /**
     * Generate unique identifier for this user
     * Uses the existing ID but ensures it's properly formatted
     */
    val uniqueIdentifier: String
        get() = "USER_${id.replace("-", "").take(8).uppercase()}"
}