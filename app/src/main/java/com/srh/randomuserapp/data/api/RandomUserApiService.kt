package com.srh.randomuserapp.data.api

import com.srh.randomuserapp.data.models.RandomUserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for randomuser.me API.
 * Provides endpoints to fetch random user data.
 */
interface RandomUserApiService {

    /**
     * Fetches multiple random users from the API
     *
     * @param results Number of users to fetch (default: 10)
     * @param format Response format (json, xml, csv, yaml)
     * @param nat Nationality filter (TO DO?)
     * @param gender Gender filter (male, female, maybe extendable?)
     * @param seed Seed for reproducible results (TO DO?)
     * @return Response containing list of random users
     */
    @GET("api/")
    suspend fun getRandomUsers(
        @Query("results") results: Int = 10,
        @Query("format") format: String = "json",
        @Query("nat") nationality: String? = null,
        @Query("gender") gender: String? = null,
        @Query("seed") seed: String? = null
    ): Response<RandomUserResponse>

    /**
     * Fetches a single random user from the API
     *
     * @param format Response format (json, xml, csv, yaml)
     * @param nat Nationality filter
     * @param gender Gender filter (male, female)
     * @param seed Seed for reproducible results
     * @return Response containing single random user
     */
    @GET("api/")
    suspend fun getRandomUser(
        @Query("results") results: Int = 1,
        @Query("format") format: String = "json",
        @Query("nat") nationality: String? = null,
        @Query("gender") gender: String? = null,
        @Query("seed") seed: String? = null
    ): Response<RandomUserResponse>

    /**
     * Fetches users with specific parameters for testing
     *
     * @param count Number of users to fetch
     * @param includeFields Comma-separated list of fields to include
     * @param excludeFields Comma-separated list of fields to exclude
     * @return Response containing filtered user data
     */
    @GET("api/")
    suspend fun getUsersWithFilter(
        @Query("results") count: Int,
        @Query("inc") includeFields: String? = null,
        @Query("exc") excludeFields: String? = null,
        @Query("format") format: String = "json"
    ): Response<RandomUserResponse>

    companion object {
        const val BASE_URL = "https://randomuser.me/"

        // Supported nationalities
        const val NAT_US = "us"
        const val NAT_GB = "gb"
        const val NAT_DE = "de"
        const val NAT_FR = "fr"
        const val NAT_ES = "es"
        const val NAT_AU = "au"
        const val NAT_CA = "ca"

        // Gender options
        const val GENDER_MALE = "male"
        const val GENDER_FEMALE = "female"

        // Format options (bonus feature support)
        const val FORMAT_JSON = "json"
        const val FORMAT_XML = "xml"
        const val FORMAT_CSV = "csv"
        const val FORMAT_YAML = "yaml"
    }
}