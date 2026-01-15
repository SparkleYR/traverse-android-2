package com.example.traverse2.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USERNAME = stringPreferencesKey("username")
        private val USER_ID = stringPreferencesKey("user_id")
        private val LAST_SOLVE_TIME = stringPreferencesKey("last_solve_time")

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val username: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME]
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val lastSolveTime: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_SOLVE_TIME]
    }

    suspend fun isLoggedInSync(): Boolean {
        return context.dataStore.data.first()[IS_LOGGED_IN] ?: false
    }

    suspend fun saveSession(username: String, userId: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USERNAME] = username
            preferences[USER_ID] = userId
        }
    }

    suspend fun updateLastSolveTime(solveTime: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SOLVE_TIME] = solveTime
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
