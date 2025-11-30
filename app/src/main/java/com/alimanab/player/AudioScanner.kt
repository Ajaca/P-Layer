package com.alimanab.player

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import com.alimanab.player.SQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class AudioScanner(private val context: Context, private val sqlManager: SQLManager) {
    // 支持的音频文件格式
    private val supportedExtensions = arrayOf(
        ".mp3", ".wav", ".flac", ".aac", ".m4a",
        ".wma", ".ogg", ".amr", ".mid", ".xmf"
    )

    // 扫描指定文件夹
    suspend fun scanFolder(folderPath: String): ScanResult = withContext(Dispatchers.IO) {
        val scannedSongs = mutableListOf<SongModel>()
        val addedCount = 0
        val existingCount = 0

        try {
            val folder = File(folderPath)
            if (!folder.exists() || !folder.isDirectory) {
                return@withContext ScanResult(false, "文件夹不存在或不是目录", 0, 0, emptyList())
            }

            // 递归扫描文件夹
            val audioFiles = findAudioFiles(folder)

            audioFiles.forEach { file ->
                // 获取音频文件信息
                val songInfo = getAudioFileInfo(file)
                if (songInfo != null) {
                    // 插入数据库
                    val result = sqlManager.insertSong(songInfo)
                    if (result != -1L) {
                        scannedSongs.add(songInfo)
                    }
                }
            }

            ScanResult(
                success = true,
                message = "扫描完成，找到 ${audioFiles.size} 个音频文件，新增 ${scannedSongs.size} 首歌曲",
                totalScanned = audioFiles.size,
                totalAdded = scannedSongs.size,
                scannedSongs = scannedSongs
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ScanResult(false, "扫描失败：${e.message}", 0, 0, emptyList())
        }
    }

    // 扫描默认音乐文件夹
    suspend fun scanDefaultMusicFolder(): ScanResult {
        val defaultPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
        return scanFolder(defaultPath)
    }

    // 递归查找音频文件
    private fun findAudioFiles(folder: File): List<File> {
        val audioFiles = mutableListOf<File>()

        folder.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                // 递归扫描子文件夹
                audioFiles.addAll(findAudioFiles(file))
            } else if (isAudioFile(file.name)) {
                audioFiles.add(file)
            }
        }

        return audioFiles
    }

    // 判断是否为音频文件
    private fun isAudioFile(fileName: String): Boolean {
        return supportedExtensions.any { fileName.lowercase(Locale.getDefault()).endsWith(it) }
    }

    // 获取音频文件的元数据（标题、艺术家、时长等）
    private fun getAudioFileInfo(file: File): SongModel? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: file.nameWithoutExtension
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "未知艺术家"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toIntOrNull() ?: 0
            val size = file.length()

            retriever.release()

            SongModel(
                title = title,
                artist = artist,
                duration = duration,
                path = file.absolutePath,
                fileName = file.name,
                size = size
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 扫描结果数据类
    data class ScanResult(
        val success: Boolean,
        val message: String,
        val totalScanned: Int,
        val totalAdded: Int,
        val scannedSongs: List<SongModel>
    )
}
