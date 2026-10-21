package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "al_hadid_user_prefs")

data class UserSession(
    val isLoggedIn: Boolean = false,
    val userId: Int = 0,
    val role: String = "Teacher",
    val name: String = "anas mustafa",
    val email: String = "",
    val phone: String = "",
    val assignedGroups: String = "",
    val rememberMe: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private val legacySharedPrefs =
        context.getSharedPreferences("al_hadid_prefs", Context.MODE_PRIVATE)

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = intPreferencesKey("user_id")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val ASSIGNED_GROUPS = stringPreferencesKey("assigned_groups")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    val userSessionFlow: Flow<UserSession> = context.userDataStore.data.map { prefs ->
        val dsRemember = prefs[Keys.REMEMBER_ME] ?: legacySharedPrefs.getBoolean("remember_me", false)
        val dsLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: legacySharedPrefs.getBoolean("is_logged_in", false)
        val dsUserId = prefs[Keys.USER_ID] ?: legacySharedPrefs.getInt("user_id", 0)
        val dsRole = prefs[Keys.USER_ROLE] ?: legacySharedPrefs.getString("user_role", "Teacher") ?: "Teacher"
        val dsName = prefs[Keys.USER_NAME] ?: legacySharedPrefs.getString("user_name", "anas mustafa") ?: "anas mustafa"
        val dsEmail = prefs[Keys.USER_EMAIL] ?: legacySharedPrefs.getString("user_email", "") ?: ""
        val dsPhone = prefs[Keys.USER_PHONE] ?: legacySharedPrefs.getString("user_phone", "") ?: ""
        val dsGroups = prefs[Keys.ASSIGNED_GROUPS] ?: legacySharedPrefs.getString("assigned_groups", "") ?: ""

        UserSession(
            isLoggedIn = dsLoggedIn,
            userId = dsUserId,
            role = dsRole,
            name = dsName,
            email = dsEmail,
            phone = dsPhone,
            assignedGroups = dsGroups,
            rememberMe = dsRemember
        )
    }

    suspend fun saveUserSession(
        userId: Int = 0,
        role: String,
        name: String,
        email: String = "",
        phone: String = "",
        assignedGroups: String = "",
        rememberMe: Boolean = false
    ) {
        val cleanRole = role.removePrefix("As a ").removePrefix("As an ").trim().ifEmpty { "Teacher" }
        val cleanName = name.trim().ifEmpty { "anas mustafa" }
        legacySharedPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putBoolean("remember_me", rememberMe)
            .putInt("user_id", userId)
            .putString("user_role", cleanRole)
            .putString("user_name", cleanName)
            .putString("user_email", email.trim())
            .putString("user_phone", phone.trim())
            .putString("assigned_groups", assignedGroups.trim())
            .apply()

        context.userDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.REMEMBER_ME] = rememberMe
            prefs[Keys.USER_ID] = userId
            prefs[Keys.USER_ROLE] = cleanRole
            prefs[Keys.USER_NAME] = cleanName
            prefs[Keys.USER_EMAIL] = email.trim()
            prefs[Keys.USER_PHONE] = phone.trim()
            prefs[Keys.ASSIGNED_GROUPS] = assignedGroups.trim()
        }
    }

    suspend fun updateAssignedGroupsInSession(assignedGroups: String) {
        legacySharedPrefs.edit()
            .putString("assigned_groups", assignedGroups.trim())
            .apply()
        context.userDataStore.edit { prefs ->
            prefs[Keys.ASSIGNED_GROUPS] = assignedGroups.trim()
        }
    }

    suspend fun updateRoleAndName(role: String, name: String) {
        val cleanRole = role.removePrefix("As a ").removePrefix("As an ").trim().ifEmpty { "Teacher" }
        val cleanName = name.trim().ifEmpty { "anas mustafa" }
        legacySharedPrefs.edit()
            .putString("user_role", cleanRole)
            .putString("user_name", cleanName)
            .apply()

        context.userDataStore.edit { prefs ->
            prefs[Keys.USER_ROLE] = cleanRole
            prefs[Keys.USER_NAME] = cleanName
        }
    }

    suspend fun logout() {
        legacySharedPrefs.edit()
            .putBoolean("is_logged_in", false)
            .putBoolean("remember_me", false)
            .apply()
        context.userDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
            prefs[Keys.REMEMBER_ME] = false
        }
    }

    fun isRememberMeEnabled(): Boolean = legacySharedPrefs.getBoolean("remember_me", false)

    // Synchronous session clear for when user presses Back to close app or app is destroyed:
    // If Remember Me NOT checked, clear login session so next time app asks for login again
    fun clearSessionOnAppCloseSync() {
        val remember = legacySharedPrefs.getBoolean("remember_me", false)
        if (!remember) {
            legacySharedPrefs.edit().putBoolean("is_logged_in", false).commit()
        }
    }
}
