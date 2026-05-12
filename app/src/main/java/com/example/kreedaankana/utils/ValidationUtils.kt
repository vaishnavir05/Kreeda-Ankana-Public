package com.example.kreedaankana.utils

object ValidationUtils {
    private val NAME_REGEX = Regex("^[a-zA-Z ]+$")
    private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+\\-]+@gmail\\.com$")
    private val PHONE_REGEX = Regex("^[0-9]{10}$")
    private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&^_\\-])[A-Za-z\\d@\$!%*#?&^_\\-]{8,}$")

    fun isValidName(name: String): Boolean = NAME_REGEX.matches(name.trim())
    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())
    fun isValidPhone(phone: String): Boolean = PHONE_REGEX.matches(phone.trim())
    fun isValidPassword(password: String): Boolean = PASSWORD_REGEX.matches(password)

    fun hashPassword(password: String): String = password.hashCode().toString()
}
