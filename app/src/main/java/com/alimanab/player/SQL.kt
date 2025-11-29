package com.alimanab.player.ui.theme

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context

class SQL(context: Context?) :
    SQLiteOpenHelper(context, "SimpleMusic.db", null, 1) {

    // 用户表
    override fun onCreate(db: SQLiteDatabase?) {
        // 创建用户表
        db?.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT
            )
        """)

        // 创建歌单表
        db?.execSQL("""
            CREATE TABLE playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                name TEXT,
                songs TEXT,  -- 用逗号分隔的歌曲名
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS playlists")
        db?.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }
}