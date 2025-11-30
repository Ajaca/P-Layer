package com.alimanab.player

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.alimanab.player.SQL

class MusicPlayerService : Service() {
    // Binder 用于 Activity 与 Service 通信
    private val binder = LocalBinder()
    // 媒体播放器实例
    private var mediaPlayer: MediaPlayer? = null
    // 当前播放列表
    private var currentPlaylist: List<SongModel> = emptyList()
    // 当前播放位置
    private var currentPosition: Int = -1

    // 播放状态枚举（替代 PlaybackStateCompat）
    enum class PlaybackState {
        STOPPED, PLAYING, PAUSED, ERROR
    }

    // 当前播放状态
    private var currentState: PlaybackState = PlaybackState.STOPPED

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化 MediaPlayer
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            // 设置播放完成监听
            setOnCompletionListener {
                playNext()
            }
            // 设置错误监听
            setOnErrorListener { _, _, _ ->
                currentState = PlaybackState.ERROR
                true
            }
        }
    }

    // 设置播放列表并播放
    fun setPlaylistAndPlay(playlist: List<SongModel>, startPosition: Int = 0) {
        if (playlist.isEmpty() || startPosition < 0 || startPosition >= playlist.size) {
            currentState = PlaybackState.ERROR
            return
        }
        currentPlaylist = playlist
        currentPosition = startPosition
        playCurrent()
    }

    // 播放当前歌曲
    private fun playCurrent() {
        if (currentPlaylist.isEmpty() || currentPosition < 0 || currentPosition >= currentPlaylist.size) {
            currentState = PlaybackState.ERROR
            return
        }

        val currentSong = currentPlaylist[currentPosition]
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(currentSong.path)
            mediaPlayer?.prepareAsync() // 异步准备（避免阻塞主线程）
            mediaPlayer?.setOnPreparedListener {
                it.start()
                currentState = PlaybackState.PLAYING
            }
        } catch (e: Exception) {
            e.printStackTrace()
            currentState = PlaybackState.ERROR
        }
    }

    // 暂停播放
    fun pause() {
        mediaPlayer?.pause()
        currentState = PlaybackState.PAUSED
    }

    // 继续播放
    fun resume() {
        if (currentState == PlaybackState.PAUSED) {
            mediaPlayer?.start()
            currentState = PlaybackState.PLAYING
        }
    }

    // 播放下一首
    fun playNext() {
        if (currentPosition < currentPlaylist.size - 1) {
            currentPosition++
            playCurrent()
        } else {
            // 播放到最后一首，停止播放
            stop()
        }
    }

    // 播放上一首
    fun playPrevious() {
        if (currentPosition > 0) {
            currentPosition--
            playCurrent()
        } else {
            // 已经是第一首，重新播放
            playCurrent()
        }
    }

    // 停止播放
    fun stop() {
        mediaPlayer?.stop()
        currentState = PlaybackState.STOPPED
        currentPosition = -1
    }

    // 获取当前播放状态
    fun getCurrentPlaybackState(): PlaybackState {
        return currentState
    }

    // 获取当前播放歌曲
    fun getCurrentSong(): SongModel? {
        return if (currentPosition >= 0 && currentPosition < currentPlaylist.size) {
            currentPlaylist[currentPosition]
        } else null
    }

    // 获取当前播放进度（毫秒）
    fun getCurrentProgress(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    // 跳转播放进度
    fun seekTo(progress: Int) {
        mediaPlayer?.seekTo(progress)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        mediaPlayer?.release()
        mediaPlayer = null
        currentState = PlaybackState.STOPPED
    }
}
