package com.srh.randomuserapp.data.models

import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension functions to map between API DTOs and database entities
 */

/**
 * Converts RandomUserDto from API to User entity for database storage
 * @param qrCode Generated QR code for this user
 * @return User entity ready for database insertion
 */
fun RandomUserDto.toUser(qrCode: String): User {
    return User(
        id = login.uuid,
        firstName = name.first.capitalizeWords(),
        lastName = name.last.capitalizeWords(),
        email = email,
        phone = phone,
        dateOfBirth = formatDate(dob.date),
        profilePictureUrl = picture.large,
        gender = gender.capitalizeWords(),
        country = location.country.capitalizeWords(),
        city = location.city.capitalizeWords(),
        street = location.street.fullStreet.capitalizeWords(),
        qrCode = qrCode,
        isManuallyCreated = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Creates a manually created user with provided data
 * @param firstName User's first name
 * @param lastName User's last name
 * @param email User's email
 * @param phone User's phone number
 * @param dateOfBirth User's birth date
 * @param profilePictureUrl URL to profile picture (optional)
 * @param gender User's gender
 * @param country User's country
 * @param city User's city
 * @param street User's street address
 * @param qrCode Generated QR code
 * @return User entity for manual user
 */
fun createManualUser(
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    dateOfBirth: String,
    profilePictureUrl: String = "",
    gender: String = "",
    country: String = "",
    city: String = "",
    street: String = "",
    qrCode: String
): User {
    return User(
        id = UUID.randomUUID().toString(),
        firstName = firstName.capitalizeWords(),
        lastName = lastName.capitalizeWords(),
        email = email,
        phone = phone,
        dateOfBirth = dateOfBirth,
        profilePictureUrl = profilePictureUrl,
        gender = gender.capitalizeWords(),
        country = country.capitalizeWords(),
        city = city.capitalizeWords(),
        street = street.capitalizeWords(),
        qrCode = qrCode,
        isManuallyCreated = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Formats ISO date string to readable format
 * @param isoDate ISO format date string
 * @return Formatted date string
 */
private fun formatDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        // Fallback to original if parsing fails
        isoDate.substringBefore('T')
    }
}

/**
 * Capitalizes first letter of each word
 * @return Capitalized string
 */
private fun String.capitalizeWords(): String {
    return this.split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
}