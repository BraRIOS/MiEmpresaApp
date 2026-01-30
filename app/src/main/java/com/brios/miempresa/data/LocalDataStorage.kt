package com.brios.miempresa.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "MIEMPRESA_DATA_STORE")

object PreferencesKeys {
    val SPREADSHEET_ID_KEY = stringPreferencesKey("spreadsheet_id_key")
}

suspend fun <T> saveToDataStore(
    context: Context,
    value: T,
    key: Preferences.Key<T>,
) {
    context.dataStore.edit { preferences ->
        preferences[key] = value
    }
}

fun <T> getFromDataStore(
    context: Context,
    key: Preferences.Key<T>,
): Flow<T?> {
    return context.dataStore.data
        .map { preferences ->
            preferences[key]
        }
}

suspend fun <T> removeValueFromDataStore(
    context: Context,
    key: Preferences.Key<T>,
) {
    context.dataStore.edit { preferences ->
        preferences.remove(key)
    }
}
