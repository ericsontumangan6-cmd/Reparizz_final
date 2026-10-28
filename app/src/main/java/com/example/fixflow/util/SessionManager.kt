package com.example.fixflow.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "reparizz_session_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"

        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_STAFF = "STAFF"
        const val ROLE_USER = "USER"
        const val ROLE_CUSTOMER = "USER"
    }

    fun saveSession(userId: Long, email: String, name: String, role: String) {
        val normalizedRole = role.uppercase().trim()
        val finalRole = when (normalizedRole) {
            "ADMIN" -> ROLE_ADMIN
            "STAFF" -> ROLE_STAFF
            else -> ROLE_USER
        }
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_ROLE, finalRole)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun isAdmin(): Boolean = isLoggedIn() && (getUserRole() == ROLE_ADMIN)

    fun isStaff(): Boolean = isLoggedIn() && (getUserRole() == ROLE_STAFF || getUserRole() == ROLE_ADMIN)

    fun isUser(): Boolean = isLoggedIn() && (getUserRole() == ROLE_USER)

    fun isCustomer(): Boolean = isUser()

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "User") ?: "User"

    fun getUserRole(): String {
        val raw = prefs.getString(KEY_USER_ROLE, ROLE_USER) ?: ROLE_USER
        return when (raw.uppercase().trim()) {
            "ADMIN" -> ROLE_ADMIN
            "STAFF" -> ROLE_STAFF
            else -> ROLE_USER
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
