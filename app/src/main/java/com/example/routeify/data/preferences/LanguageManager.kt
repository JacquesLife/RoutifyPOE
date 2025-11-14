package com.example.routeify.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.routeify.R
import java.util.Locale

class LanguageManager(private val context: Context){
    private val prefs: SharedPreferences = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    var currentLanguage by mutableStateOf(getSavedLanguage())
        private set

    // Use Int state for triggering recomposition and activity restart
    var languageChangeTrigger by mutableIntStateOf(0)
        private set

    companion object{
        const val KEY_LANGUAGE = "selected_language"
        const val ENGLISH = "en"
        const val AFRIKAANS = "af"

        @Volatile
        private var instance: LanguageManager? = null

        fun getInstance(context: Context): LanguageManager {
            return instance ?: synchronized(this) {
                instance ?: LanguageManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // Retrieve the saved language
    fun getSavedLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, ENGLISH) ?: ENGLISH
    }

    // Set & save the selected language
    fun setLanguage(languageCode: String) {
        val previousLanguage = currentLanguage
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        currentLanguage = languageCode
        updateLocale(languageCode)

        // Only trigger if language actually changed
        if (previousLanguage != languageCode) {
            languageChangeTrigger++
        }
    }

    // Update app locale
    private fun updateLocale(languageCode: String){
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    // Get the name of the language
    fun getLanguageName(languageCode: String): String {
        return when(languageCode){
            ENGLISH -> context.getString(R.string.language_english)
            AFRIKAANS -> context.getString(R.string.language_afrikaans)
            else -> context.getString(R.string.language_english)
        }
    }
}