package com.example.fixflow.util

import java.security.MessageDigest

object PasswordHasher {

    /**
     * Computes a SHA-256 hash of the input password.
     * Uses a fixed salt string to ensure security against plain rainbow tables.
     */
    fun hashPassword(password: String): String {
        if (password.isEmpty()) return ""
        val salt = "FixFlow_RepaRizz_Salt_2025#"
        val saltedInput = salt + password
        val bytes = MessageDigest.getInstance("SHA-256").digest(saltedInput.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies if a raw password matches a stored password hash.
     * Also supports fallback matching for legacy unhashed passwords during migration.
     */
    fun verifyPassword(rawPassword: String, storedHash: String): Boolean {
        if (rawPassword.isEmpty() || storedHash.isEmpty()) return false
        val computedHash = hashPassword(rawPassword)
        return computedHash.equals(storedHash, ignoreCase = true) || rawPassword == storedHash
    }
}
