package com.example.finalprojectinnobridge.utils

import android.util.Patterns

object Validator {
    fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    fun validateName(name: String): Boolean {
        return name.isNotEmpty()
    }
}