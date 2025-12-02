package com.alimanab.player
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LightMusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val binder = SimpleBinder()

    // 简化的 Binder
    inner class SimpleBinder : Binder() {
        fun getService() = this@LightMusicService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnCompletionListener {
                // 播放完成通知，由 ViewModel 处理下一首
                sendBroadcast(Intent("PLAYBACK_COMPLETED"))
            }
        }
    }

    // 暴露最基本的控制方法
    fun play(songPath: String) {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(songPath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() = mediaPlayer?.pause()
    fun resume() = mediaPlayer?.start()
    fun stop() = mediaPlayer?.stop()
    fun seekTo(position: Int) = mediaPlayer?.seekTo(position)
    fun isPlaying() = mediaPlayer?.isPlaying ?: false
    fun getCurrentPosition() = mediaPlayer?.currentPosition ?: 0

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

class PlayerManager(private val context: Context) {
    private var musicService: LightMusicService? = null
    private var isBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LightMusicService.SimpleBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            musicService = null
        }
    }

    fun startService() {
        val intent = Intent(context, LightMusicService::class.java)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun play(songPath: String) {
        if (!isBound) startService()
        musicService?.play(songPath)
    }

    fun pause() = musicService?.pause()
    fun resume() = musicService?.resume()
    fun stop() = musicService?.stop()
    fun seekTo(position: Int) = musicService?.seekTo(position)
    fun isPlaying() = musicService?.isPlaying() ?: false
    fun getCurrentPosition() = musicService?.getCurrentPosition() ?: 0

    fun release() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}


class LightPlayerViewModel : ViewModel() {
    private var playerManager: PlayerManager? = null
    private var sqlManager: SQLManager? = null

    // 状态
    var currentPlaylist by mutableStateOf(emptyList<SongModel>())
    var currentSongIndex by mutableStateOf(-1)
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableStateOf(0)

    // 初始化（手动传入 context）
    fun initialize(context: Context) {
        playerManager = PlayerManager(context.applicationContext)
        sqlManager = SQLManager(context.applicationContext)

        // 启动进度更新
        viewModelScope.launch {
            while (true) {
                if (isPlaying) {
                    currentPosition = playerManager?.getCurrentPosition() ?: 0
                }
                delay(1000)
            }
        }
    }
}
