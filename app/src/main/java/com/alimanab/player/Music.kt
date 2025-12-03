package com.alimanab.player
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random


fun simplePlay(filePath: String) {
    MediaPlayer().apply {
        setDataSource(filePath)
        setAudioStreamType(AudioManager.STREAM_MUSIC)
        prepare()
        start()
    }
}

class LightMusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val binder = LocalBinder()
    private var currentSongPath: String? = null

    inner class LocalBinder : Binder() {
        fun getService() = this@LightMusicService
    }

    companion object {
        var instance: LightMusicService? = null
        val instanceLiveData = MutableLiveData<LightMusicService?>()
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.e("MusicPlayer", "!!!!!!!!!! LightMusicService onCreate CALLED !!!!!!!!!!")
        instance = this
        mediaPlayer = MediaPlayer()
        setupMediaPlayer()

        instanceLiveData.value = null
        instanceLiveData.value = this
    }

    private fun setupMediaPlayer() {
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )

        mediaPlayer?.setOnCompletionListener {
            sendBroadcast(Intent("PLAYBACK_COMPLETED"))
        }
    }

    fun play(songPath: String) {
        try {
            Log.d("MusicPlayer", "LightMusicService.play() called with: $songPath")

            mediaPlayer?.reset()  // 重置MediaPlayer

            mediaPlayer?.setDataSource(songPath)  // 设置音频文件路径
            mediaPlayer?.prepare()  // 或者使用 prepareAsync()，但要注意异步

            mediaPlayer?.start()  // 开始播放
            Log.d("MusicPlayer", "✅ MediaPlayer Started!")
        } catch (e: Exception) {
            Log.e("MusicPlayer", "❌ Failed to play audio: ${e.message}", e)
        }
    }

    fun pause() = mediaPlayer?.pause()
    fun resume() = mediaPlayer?.start()
    fun stop() = mediaPlayer?.stop()
    fun seekTo(position: Int) = mediaPlayer?.seekTo(position)
    fun isPlaying() = mediaPlayer?.isPlaying ?: false
    fun getCurrentPosition() = mediaPlayer?.currentPosition ?: 0
    fun getDuration() = mediaPlayer?.duration ?: 0

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        instance = null
    }
}

class PlayerManager(private val context: Context) {
    private var musicService: LightMusicService? = null
    private var isBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            //musicService = (service as LightMusicService.LocalBinder).getService()
            musicService = service.javaClass.getMethod("getService").invoke(service) as LightMusicService
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            musicService = null
        }
    }

    fun bindService() {
        Log.d("MusicPlayer", "startService() called")
        val intent = Intent(context, LightMusicService::class.java)
        val bindResult = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Log.d("MusicPlayer", "bindService result: $bindResult")

        context.startService(intent)
    }
    private var pendingPlayPath: String? = null

    // 🎯 用于防止重复启动 Service
    private var hasStartedService = false

    fun play(songPath: String) {
        Log.d("MusicPlayer", "play() called with path: $songPath")

        if (LightMusicService.instance != null) {
            Log.d("MusicPlayer", "✅ Service already exists, playing directly")
            LightMusicService.instance?.play(songPath)
            return
        }

        if (!hasStartedService) {
            Log.d("MusicPlayer", "🚀 Starting LightMusicService for the first time")
            val intent = Intent(context, LightMusicService::class.java)
            context.startService(intent)
            hasStartedService = true
        }

        pendingPlayPath = songPath

        // 🎯 先尝试直接播放（可能 Service 已经创建但 LiveData 没通知）
        if (LightMusicService.instance != null) {
            Log.d("MusicPlayer", "✅ Instance already available, playing immediately")
            LightMusicService.instance?.play(songPath)
            pendingPlayPath = null
            return
        }

        // 🎯 否则观察 LiveData
        LightMusicService.instanceLiveData.observeForever(object : Observer<LightMusicService?> {
            override fun onChanged(service: LightMusicService?) {
                if (service != null) {
                    Log.d("MusicPlayer", "✅ Observed Service is ready, playing pending song")
                    pendingPlayPath?.let { path ->
                        service.play(path)
                        pendingPlayPath = null
                    }
                    LightMusicService.instanceLiveData.removeObserver(this)
                }
            }
        })
    }

    fun pause() = musicService?.pause()
    fun resume() = musicService?.resume()
    fun stop() = musicService?.stop()
    fun seekTo(position: Int) = musicService?.seekTo(position)
    fun isPlaying() = musicService?.isPlaying() ?: false
    fun getCurrentPosition() = musicService?.getCurrentPosition() ?: 0
    fun getDuration() = musicService?.getDuration() ?: 0

    fun unbind() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}

class LightPlayerViewModel : ViewModel() {
    private var playerManager: PlayerManager? = null
    private var progressJob: Job? = null

    // 状态
    var currentPlaylist by mutableStateOf(emptyList<SongModel>())
    var currentSongIndex by mutableStateOf(-1)
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableStateOf(0)
    var currentSongDuration by mutableStateOf(0)

    enum class PlayMode { SEQUENTIAL, RANDOM, SINGLE_LOOP }
    var playMode by mutableStateOf(PlayMode.SEQUENTIAL)

    fun init(context: Context, playlist: List<SongModel>, startSong: SongModel? = null) {
        playerManager = PlayerManager(context.applicationContext)
        currentPlaylist = playlist

        startSong?.let { song ->
            currentSongIndex = playlist.indexOfFirst { it.path == song.path }.takeIf { it >= 0 }
                ?: run {
                    currentPlaylist = listOf(song) + playlist
                    0
                }
        } ?: run {
            if (playlist.isNotEmpty()) currentSongIndex = 0
        }

        startProgressUpdate()
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                if (isPlaying) {
                    currentPosition = playerManager?.getCurrentPosition() ?: 0
                    if (currentSongDuration == 0) {
                        currentSongDuration = playerManager?.getDuration() ?: 0
                    }
                }
                delay(500)
            }
        }
    }

    fun playCurrent() {

        val song = getCurrentSong() ?: return
        Log.d("MusicPlayer", "Playing: ${song.path}")

        playerManager?.play(song.path)
        isPlaying = true

        // 延迟获取时长
        viewModelScope.launch {
            delay(300)
            currentSongDuration = playerManager?.getDuration() ?: 0
        }
    }

    fun togglePlayPause() {
        Log.d("MusicPlayer", "togglePlayPause called, isPlaying: $isPlaying")
        if (isPlaying) {
            playerManager?.pause()
            isPlaying = false
        } else {
            Log.d("MusicPlayer", "Calling playCurrent")
            playCurrent()  // 这里调用 playCurrent()
        }
    }
    fun togglePlayPauseTo(isPlay : Boolean){
        if (isPlaying != isPlay){
            togglePlayPause()
        }
    }

    fun playNext() {
        if (currentPlaylist.isEmpty()) return

        val nextIndex = when (playMode) {
            PlayMode.SEQUENTIAL -> (currentSongIndex + 1) % currentPlaylist.size
            PlayMode.RANDOM -> Random.nextInt(currentPlaylist.size)
            PlayMode.SINGLE_LOOP -> currentSongIndex
        }
        playSongAtIndex(nextIndex)
    }

    fun playPrevious() {
        if (currentPlaylist.isEmpty()) return

        val prevIndex = when (playMode) {
            PlayMode.SEQUENTIAL -> if (currentSongIndex > 0) currentSongIndex - 1 else currentPlaylist.size - 1
            PlayMode.RANDOM -> Random.nextInt(currentPlaylist.size)
            PlayMode.SINGLE_LOOP -> currentSongIndex
        }
        playSongAtIndex(prevIndex)
    }

    fun playSongAtIndex(index: Int) {
        if (index in currentPlaylist.indices) {
            currentSongIndex = index
            playCurrent()
        }
    }

    fun seekToProgress(progress: Float) {
        if (currentSongDuration > 0) {
            seekTo((currentSongDuration * progress).toInt())
        }
    }

    fun seekTo(position: Int) {
        playerManager?.seekTo(position)
        currentPosition = position
    }

    fun getCurrentSong(): SongModel? {
        return currentPlaylist.getOrNull(currentSongIndex)
    }

    fun clearPlaylist() {
        currentPlaylist = emptyList()
        currentSongIndex = -1
        isPlaying = false
        currentPosition = 0
        currentSongDuration = 0
        playerManager?.stop()
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        playerManager?.unbind()
    }
}