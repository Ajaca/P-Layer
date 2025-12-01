package com.alimanab.player

object AccountManager {
    fun AccountVerify(account : String) : Boolean {
        return account.isNotEmpty()
    }

    fun PasswordVerify(password : String) : Boolean {
        return (password.length >= 4 && password.matches(Regex("^[a-zA-Z0-9]*$")))
    }
}