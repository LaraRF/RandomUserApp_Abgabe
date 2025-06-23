package com.srh.randomuserapp.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

/**
 * Utility class for managing app locale and language settings.
 * Fixed version that properly persists language changes (hopefully? lol).
 */
object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    /**
     * Set the app locale and persist it
     * @param context Application context
     * @param languageCode Language code (e.g., "en", "de")
     * @return Updated context with new locale
     */
    fun setLocale(context: Context, languageCode: String): Context {
        persist(context, languageCode)
        return updateResourcesLocale(context, languageCode)
    }

    /**
     * Get current locale language code
     * @param context Application context
     * @return Current language code
     */
    fun getCurrentLanguage(context: Context): String {
        val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return preferences.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }

    /**
     * Apply saved locale on app start
     * @param context Application context
     * @return Context with applied locale
     */
    fun onAttach(context: Context): Context {
        val lang = getCurrentLanguage(context)
        return updateResourcesLocale(context, lang)
    }

    /**
     * Persist language selection
     */
    private fun persist(context: Context, languageCode: String) {
        val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        preferences.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }

    /**
     * Update resources with new locale
     */
    private fun updateResourcesLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }

    /**
     * Check if a language is supported
     * @param languageCode Language code to check
     * @return True if supported, false otherwise
     */
    fun isSupportedLanguage(languageCode: String): Boolean {
        return languageCode in listOf("en", "de")
    }

    /**
     * Get display name for a language code
     * @param languageCode Language code
     * @return Human-readable language name
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "de" -> "Deutsch"
            else -> languageCode.uppercase()
        }
    }
}