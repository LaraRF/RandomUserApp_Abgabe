package com.srh.randomuserapp.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

/**
 * Utility class for managing app locale and language settings.
 */
object LocaleHelper {

    /**
     * Set the app locale
     * @param context Application context
     * @param languageCode Language code (e.g., "en", "de")
     * @return Updated context with new locale
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Get current locale language code
     * @param context Application context
     * @return Current language code
     */
    fun getCurrentLanguage(context: Context): String {
        return context.resources.configuration.locales[0].language
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