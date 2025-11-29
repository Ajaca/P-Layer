import android.content.Context
import com.alimanab.player.ui.theme.SQL

class SQLManager(context: Context) {
    private val dbHelper = SQL(context)

    fun register(username: String, password: String): Boolean {
        val db = dbHelper.writableDatabase
        return try {
            val sql = "INSERT INTO users (username, password) VALUES (?, ?)"
            db.execSQL(sql, arrayOf(username, password))
            true
        } catch (e: Exception) {
            false
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
        return success
    }

    // 创建歌单
    fun createPlaylist(userId: Int, playlistName: String): Boolean {
        val db = dbHelper.writableDatabase
        return try {
            val sql = "INSERT INTO playlists (user_id, name, songs) VALUES (?, ?, ?)"
            db.execSQL(sql, arrayOf(userId, playlistName, "")) // 初始无歌曲
            true
        } catch (e: Exception) {
            false
        }
    }

    // 添加歌曲到歌单（用逗号分隔存储）
    fun addSong(playlistId: Int, songName: String): Boolean {
        val db = dbHelper.writableDatabase

        // 先获取现有歌曲
        val cursor = db.rawQuery(
            "SELECT songs FROM playlists WHERE id=?",
            arrayOf(playlistId.toString())
        )

        return if (cursor.moveToFirst()) {
            val currentSongs = cursor.getString(0)
            val newSongs = if (currentSongs.isEmpty()) {
                songName
            } else {
                "$currentSongs,$songName"  // 用逗号分隔
            }

            cursor.close()

            // 更新歌单
            val updateSql = "UPDATE playlists SET songs=? WHERE id=?"
            db.execSQL(updateSql, arrayOf(newSongs, playlistId.toString()))
            true
        } else {
            cursor.close()
            false
        }
    }

    // 获取用户的所有歌单
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
        return playlists
    }

    // 获取歌单的所有歌曲
    fun getPlaylistSongs(playlistName: String): List<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT songs FROM playlists WHERE name=?",
            arrayOf(playlistName)
        )

        return if (cursor.moveToFirst()) {
            val songsText = cursor.getString(0)
            cursor.close()
            if (songsText.isEmpty()) {
                emptyList()
            } else {
                songsText.split(",") // 按逗号分割
            }
        } else {
            cursor.close()
            emptyList()
        }
    }
}