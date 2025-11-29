package com.alimanab.player

import android.content.Context
import android.content.SharedPreferences

object IOBundle {
    inline fun <reified T> save(context: Context, fileName: String, key: String, value: T)
    = with(context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit()) {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Long -> putLong(key, value)
            is Set<*> -> putStringSet(key, value.filterIsInstance<String>().toSet())
        }
        apply()
    }

    inline fun <reified T> get(context: Context, fileName: String, key: String, defaultValue: T)
    = context.getSharedPreferences(fileName, Context.MODE_PRIVATE).let {
        when (defaultValue) {
            is String -> it.getString(key, defaultValue) ?: defaultValue
            is Int -> it.getInt(key, defaultValue)
            is Boolean -> it.getBoolean(key, defaultValue)
            is Float -> it.getFloat(key, defaultValue)
            is Long -> it.getLong(key, defaultValue)
            is Set<*> -> it.getStringSet(key, defaultValue.filterIsInstance<String>().toSet()) ?: defaultValue
            else -> defaultValue
        }
    }
}