package cc.vastsea.zrll.ncmdumpercompose.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private const val PREFERENCES_NAME = "settings"

private val Context.dataStore by preferencesDataStore(name = PREFERENCES_NAME)

object PreferencesKeys {
    val INPUT_DIR = stringPreferencesKey("input_dir")
    val OUTPUT_DIR = stringPreferencesKey("output_dir")
}



class PreferencesManager(private val context: Context) {
    val inputDirFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.INPUT_DIR]
    }

    val outputDirFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.OUTPUT_DIR]
    }

    suspend fun saveInputDir(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INPUT_DIR] = name
        }
    }

    suspend fun saveOutputDir(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OUTPUT_DIR] = name
        }
    }
}