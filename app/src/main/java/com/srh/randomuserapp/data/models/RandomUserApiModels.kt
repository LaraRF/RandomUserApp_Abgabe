package com.srh.randomuserapp.data.models

import com.google.gson.annotations.SerializedName

/**
 * Main response wrapper from randomuser.me API
 */
data class RandomUserResponse(
    val results: List<RandomUserDto>,
    val info: ApiInfo
)

/**
 * Individual user data from API
 */
data class RandomUserDto(
    val gender: String,
    val name: NameDto,
    val location: LocationDto,
    val email: String,
    val login: LoginDto,
    val dob: DateOfBirthDto,
    val registered: RegisteredDto,
    val phone: String,
    val cell: String,
    val id: IdDto,
    val picture: PictureDto,
    val nat: String
)

/**
 * User name information
 */
data class NameDto(
    val title: String,
    val first: String,
    val last: String
)

/**
 * User location information
 */
data class LocationDto(
    val street: StreetDto,
    val city: String,
    val state: String,
    val country: String,
    val postcode: String,
    val coordinates: CoordinatesDto,
    val timezone: TimezoneDto
)

/**
 * Street address details
 */
data class StreetDto(
    val number: Int,
    val name: String
) {
    val fullStreet: String
        get() = "$number $name"
}

/**
 * Geographic coordinates
 */
data class CoordinatesDto(
    val latitude: String,
    val longitude: String
)

/**
 * Timezone information
 */
data class TimezoneDto(
    val offset: String,
    val description: String
)

/**
 * User login credentials
 */
data class LoginDto(
    val uuid: String,
    val username: String,
    val password: String,
    val salt: String,
    val md5: String,
    val sha1: String,
    val sha256: String
)

/**
 * Date of birth information
 */
data class DateOfBirthDto(
    val date: String,
    val age: Int
)

/**
 * Registration date information
 */
data class RegisteredDto(
    val date: String,
    val age: Int
)

/**
 * Government ID information
 */
data class IdDto(
    val name: String?,
    val value: String?
)

/**
 * Profile picture URLs
 */
data class PictureDto(
    val large: String,
    val medium: String,
    val thumbnail: String
)

/**
 * API response metadata
 */
data class ApiInfo(
    val seed: String,
    val results: Int,
    val page: Int,
    val version: String
)