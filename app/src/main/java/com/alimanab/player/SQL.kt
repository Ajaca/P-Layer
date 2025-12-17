package com.alimanab.player

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class SQL(context: Context?) : SQLiteOpenHelper(context, "SimpleMusic.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        // 创建用户表
        db?.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT
            )
        """)

        // 创建歌单表（不含 songs 字段，用关联表替代）
        db?.execSQL("""
            CREATE TABLE playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                name TEXT,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
        """)

        // 创建歌曲表（存储音频文件详细信息）
        db?.execSQL("""
            CREATE TABLE songs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                artist TEXT DEFAULT 'V.A',
                duration INTEGER,  -- 时长（毫秒）
                path TEXT UNIQUE,  -- 文件绝对路径（唯一约束）
                file_name TEXT,    -- 文件名（如 "xxx.mp3"）
                size LONG          -- 文件大小（字节）
            )
        """)

        // 创建歌单-歌曲关联表（多对多关系，支持一个歌单多首歌、一首歌多歌单）
        db?.execSQL("""
            CREATE TABLE playlist_songs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                playlist_id INTEGER,
                song_id INTEGER,
                FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
                FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE,
                UNIQUE(playlist_id, song_id)  -- 避免重复添加同一歌曲到歌单
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // 1. 先创建新表（歌曲表和关联表）
            db?.execSQL("""
                CREATE TABLE songs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT,
                    artist TEXT DEFAULT 'V.A',
                    duration INTEGER,
                    path TEXT UNIQUE,
                    file_name TEXT,
                    size LONG
                )
            """)

            db?.execSQL("""
                CREATE TABLE playlist_songs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    playlist_id INTEGER,
                    song_id INTEGER,
                    FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
                    FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE,
                    UNIQUE(playlist_id, song_id)
                )
            """)

            // 2. 迁移旧 playlists 表（删除 songs 字段，兼容低版本 SQLite）
            // 步骤：创建临时表 → 复制旧数据 → 删除旧表 → 重命名临时表
            db?.execSQL("""
                CREATE TABLE playlists_temp (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    name TEXT,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                )
            """)

            // 复制旧表数据
            db?.execSQL("INSERT INTO playlists_temp (id, user_id, name) SELECT id, user_id, name FROM playlists")

            // 删除旧表
            db?.execSQL("DROP TABLE IF EXISTS playlists")

            db?.execSQL("ALTER TABLE playlists_temp RENAME TO playlists")
        }
    }
}

class SQLManager(context: Context) {
    private val dbHelper = SQL(context)

    fun register(username: String, password: String): Boolean {
        val db = dbHelper.writableDatabase
        return try {
            val sql = "INSERT INTO users (username, password) VALUES (?, ?)"
            db.execSQL(sql, arrayOf(username, password))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun login(username: String, password: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE username=? AND password=?",
            arrayOf(username, password)
        )
        val success = cursor.count > 0
        cursor.close()
        db.close()
        return success
    }

    fun getUserIdByUsername(username: String): Int {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "users",
            arrayOf("id"),
            "username = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        return try {
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                -1  // 未找到用户返回 -1
            }
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun createPlaylist(userId: Int, playlistName: String): Boolean {
        val db = dbHelper.writableDatabase
        return try {
            val sql = "INSERT INTO playlists (user_id, name) VALUES (?, ?)"
            db.execSQL(sql, arrayOf(userId, playlistName))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun getUserPlaylists(userId: Int): List<String> {
        val playlists = mutableListOf<String>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM playlists WHERE user_id=?",
            arrayOf(userId.toString())
        )
        while (cursor.moveToNext()) {
            playlists.add(cursor.getString(0))
        }
        cursor.close()
        db.close()
        return playlists
    }

    fun getPlaylistIdByName(userId: Int, playlistName: String): Int {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "playlists",
            arrayOf("id"),
            "user_id = ? AND name = ?",
            arrayOf(userId.toString(), playlistName),
            null,
            null,
            null
        )
        return try {
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                -1  // 未找到歌单返回 -1
            }
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun deletePlaylistById(userId: Int, playlistId: Int): Boolean {
        val db = dbHelper.writableDatabase
        return try {
            val cursor = db.query(
                "playlists",
                arrayOf("id"),
                "id = ? AND user_id = ?",
                arrayOf(playlistId.toString(), userId.toString()),
                null, null, null
            )

            val exists = cursor.count > 0
            cursor.close()

            if (!exists) {
                return false
            }

            val deletedRows = db.delete(
                "playlists",
                "id = ?",
                arrayOf(playlistId.toString())
            )

            deletedRows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun isPlaylistExists(playlistId: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM playlists WHERE id = ?",
            arrayOf(playlistId.toString())
        )
        return try {
            cursor.moveToFirst()
            cursor.getInt(0) > 0
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun insertSong(song: SongModel): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", song.title)
            put("artist", song.artist)
            put("duration", song.duration)
            put("path", song.path)
            put("file_name", song.fileName)
            put("size", song.size)
        }
        return try {
            db.insertWithOnConflict("songs", null, values, SQLiteDatabase.CONFLICT_IGNORE)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        } finally {
            db.close()
        }
    }

    fun getSongIdByPath(path: String): Int {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "songs",
            arrayOf("id"),
            "path = ?",
            arrayOf(path),
            null,
            null,
            null
        )
        return try {
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                -1  // 未找到歌曲返回 -1
            }
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun getAllSongs(): List<SongModel> {
        val songs = mutableListOf<SongModel>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query("songs", null, null, null, null, null, "title ASC")

        while (cursor.moveToNext()) {
            songs.add(
                SongModel(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    artist = cursor.getString(cursor.getColumnIndexOrThrow("artist")),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration")),
                    path = cursor.getString(cursor.getColumnIndexOrThrow("path")),
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow("file_name")),
                    size = cursor.getLong(cursor.getColumnIndexOrThrow("size"))
                )
            )
        }
        cursor.close()
        db.close()
        return songs
    }

    fun addSongToPlaylist(playlistId: Int, songId: Int): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("playlist_id", playlistId)
            put("song_id", songId)
        }
        return try {
            // 避免重复添加同一歌曲到同一歌单
            val result = db.insertWithOnConflict(
                "playlist_songs",
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )
            result != -1L  // 插入成功返回 true，重复返回 false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun getPlaylistSongs(playlistId: Int): List<SongModel> {
        val songs = mutableListOf<SongModel>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT s.* FROM songs s
            JOIN playlist_songs ps ON s.id = ps.song_id
            WHERE ps.playlist_id = ?
            ORDER BY ps.id ASC  -- 按添加顺序排序
        """.trimIndent()

        val cursor: Cursor = db.rawQuery(query, arrayOf(playlistId.toString()))
        while (cursor.moveToNext()) {
            songs.add(
                SongModel(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    artist = cursor.getString(cursor.getColumnIndexOrThrow("artist")),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration")),
                    path = cursor.getString(cursor.getColumnIndexOrThrow("path")),
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow("file_name")),
                    size = cursor.getLong(cursor.getColumnIndexOrThrow("size"))
                )
            )
        }
        cursor.close()
        db.close()
        return songs
    }

    fun removeSongFromPlaylist(playlistId: Int, songId: Int): Boolean {
        val db = dbHelper.writableDatabase
        return try {
            val deletedRows = db.delete(
                "playlist_songs",
                "playlist_id = ? AND song_id = ?",
                arrayOf(playlistId.toString(), songId.toString())
            )
            deletedRows > 0  // 删除成功返回 true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }
    fun isSongExists(path: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM songs WHERE path = ?",
            arrayOf(path)
        )
        return try {
            cursor.moveToFirst()
            cursor.getInt(0) > 0
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun getSongCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM songs", null)
        return try {
            cursor.moveToFirst()
            cursor.getInt(0)
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun isSongInPlaylist(playlistId: Int, songId: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = ? AND song_id = ?",
            arrayOf(playlistId.toString(), songId.toString())
        )
        return try {
            cursor.moveToFirst()
            cursor.getInt(0) > 0
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun getListSongsFlow(listID : Int): Flow<List<SongModel>> = flow {
        while (true) {
            emit(getPlaylistSongs(listID))
            delay(1000) // 每秒检查一次
        }
    }.distinctUntilChanged()

    fun getAllSongsFlow(): Flow<List<SongModel>> = flow {
        while (true) {
            emit(getAllSongs())
            delay(1000) // 每秒检查一次
        }
    }.distinctUntilChanged()

}

// 歌曲数据模型
data class SongModel(
    val id: Int = -1,          // 数据库主键（默认 -1 表示未插入数据库）
    val title: String,         // 歌曲标题
    val artist: String,        // 艺术家（默认 V.A）
    val duration: Int,         // 时长（毫秒）
    val path: String,          // 文件绝对路径（唯一标识）
    val fileName: String,      // 文件名（含后缀）
    val size: Long             // 文件大小（字节）
)

data class ListModel(
    val name : String,
    val id : Int,
    val owner : Int
)
