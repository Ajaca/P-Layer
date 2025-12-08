package com.alimanab.player
import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    // 播放状态
    private var playlist = mutableListOf<SongModel>()
    private var currentIndex = -1
    private var playMode = PlayMode.SEQUENTIAL

    enum class PlayMode { SEQUENTIAL, RANDOM, SINGLE_LOOP }

    // 暴露给外部的 LiveData（可选）
    private val _currentSongLiveData = MutableLiveData<SongModel?>()
    val currentSongLiveData: LiveData<SongModel?> get() = _currentSongLiveData

    private val _isPlayingLiveData = MutableLiveData<Boolean>()
    val isPlayingLiveData: LiveData<Boolean> get() = _isPlayingLiveData

    private val _progressLiveData = MutableLiveData<Pair<Int, Int>>() // current, duration
    val progressLiveData: LiveData<Pair<Int, Int>> get() = _progressLiveData

    private var progressJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService() = this@LightMusicService
    }

    companion object {
        var instance: LightMusicService? = null
        val instanceLiveData = MutableLiveData<LightMusicService?>()
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("MusicPlayer", "LightMusicService onCreate CALLED")
        instance = this
        mediaPlayer = MediaPlayer()
        setupMediaPlayer()
        instanceLiveData.postValue(this)

        startProgressUpdater()
    }

    private fun setupMediaPlayer() {
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )

        mediaPlayer?.setOnCompletionListener {
            handlePlaybackCompleted()
        }

        mediaPlayer?.setOnPreparedListener {
            mediaPlayer?.start()
            _isPlayingLiveData.postValue(true)
        }
    }

    private fun startProgressUpdater() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if (mediaPlayer?.isPlaying == true && mediaPlayer?.duration ?: 0 > 0) {
                    val pos = mediaPlayer?.currentPosition ?: 0
                    val dur = mediaPlayer?.duration ?: 0
                    _progressLiveData.postValue(pos to dur)
                }
                delay(500)
            }
        }
    }

    // ========== 对外提供的控制方法 ==========

    fun setPlaylist(list: List<SongModel>, startIndex: Int = 0, mode: PlayMode = PlayMode.SEQUENTIAL) {
        playlist = list.toMutableList()
        currentIndex = startIndex.coerceIn(0, list.size - 1)
        playMode = mode
        playCurrent()
    }

    fun playCurrent() {
        val song = getCurrentSong() ?: return
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(song.path)
            mediaPlayer?.prepareAsync() // 异步准备，避免阻塞主线程
            _currentSongLiveData.postValue(song)
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Play failed: ${e.message}", e)
        }
    }

    fun playNext() {
        if (playlist.isEmpty()) return
        val nextIndex = when (playMode) {
            PlayMode.SEQUENTIAL -> (currentIndex + 1) % playlist.size
            PlayMode.RANDOM -> Random.nextInt(playlist.size)
            PlayMode.SINGLE_LOOP -> currentIndex
        }
        playSongAtIndex(nextIndex)
    }

    fun playPrevious() {
        if (playlist.isEmpty()) return
        val prevIndex = when (playMode) {
            PlayMode.SEQUENTIAL -> if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
            PlayMode.RANDOM -> Random.nextInt(playlist.size)
            PlayMode.SINGLE_LOOP -> currentIndex
        }
        playSongAtIndex(prevIndex)
    }

    fun playSongAtIndex(index: Int) {
        if (index in playlist.indices) {
            currentIndex = index
            playCurrent()
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            _isPlayingLiveData.postValue(false)
        }
    }

    fun resume() {
        if (mediaPlayer?.isPlaying == false && (mediaPlayer?.currentPosition
                ?: 0) < (mediaPlayer?.duration ?: 0)
        ) {
            mediaPlayer?.start()
            _isPlayingLiveData.postValue(true)
        } else if (!mediaPlayer?.isPlaying!! && playlist.isNotEmpty()) {
            playCurrent()
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        _isPlayingLiveData.postValue(false)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _progressLiveData.postValue(position to (mediaPlayer?.duration ?: 0))
    }

    fun isPlaying() = mediaPlayer?.isPlaying ?: false
    fun getCurrentPosition() = mediaPlayer?.currentPosition ?: 0
    fun getDuration() = mediaPlayer?.duration ?: 0
    fun getCurrentSong() = playlist.getOrNull(currentIndex)

    private fun handlePlaybackCompleted() {
        when (playMode) {
            PlayMode.SEQUENTIAL, PlayMode.RANDOM -> playNext()
            PlayMode.SINGLE_LOOP -> {
                mediaPlayer?.seekTo(0)
                mediaPlayer?.start()
                _isPlayingLiveData.postValue(true)
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 设置为前台服务（推荐用于音乐播放）
        // startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY // 被杀死后尽量重启
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        progressJob?.cancel()
        instance = null
        instanceLiveData.postValue(null)
    }
}

@SuppressLint("StaticFieldLeak")
object PlayerManager {
    private var musicService: LightMusicService? = null
    private var isBound = false
    private lateinit var context: Context
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        this.context = context.applicationContext
        isInitialized = true
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LightMusicService.LocalBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            musicService = null
        }
    }

    fun bind() {
        if (!isBound) {
            // 此时 context 已通过 init 初始化，可安全使用
            val intent = Intent(context, LightMusicService::class.java)
            val bindResult = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.d("MusicPlayer", "bindService 返回值: $bindResult") // 验证绑定是否发起成功
        } else {
            Log.d("MusicPlayer", "已经绑定过了")
        }
    }

    fun setPlaylist(list: List<SongModel>, startIndex: Int = 0, mode: LightMusicService.PlayMode = LightMusicService.PlayMode.SEQUENTIAL) {
        musicService?.setPlaylist(list, startIndex, mode)
        //Toast.makeText(context,"Initialized? $musicService", Toast.LENGTH_SHORT).show()
    }

    fun playNext() = musicService?.playNext()
    fun playPrevious() = musicService?.playPrevious()
    fun playSongAtIndex(index: Int) = musicService?.playSongAtIndex(index)
    fun pause() = musicService?.pause()
    fun resume() = musicService?.resume()
    fun stop() = musicService?.stop()
    fun seekTo(position: Int) = musicService?.seekTo(position)
    fun isPlaying() = musicService?.isPlaying() ?: false
    fun getCurrentPosition() = musicService?.getCurrentPosition() ?: 0
    fun getDuration() = musicService?.getDuration() ?: 0
    fun getCurrentSong() = musicService?.getCurrentSong()
}

class PlayerViewModel : ViewModel() {

    private var progressJob: Job? = null

    var currentPlaylist by mutableStateOf(emptyList<SongModel>())
    var currentSongIndex by mutableStateOf(-1)
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableStateOf(0)
    var currentSongDuration by mutableStateOf(0)

    enum class PlayMode { SEQUENTIAL, RANDOM, SINGLE_LOOP }
    var playModes by mutableStateOf(PlayMode.SEQUENTIAL)

    // 监听 Service 状态
    private val service = LightMusicService.instanceLiveData

    init {
        observeService()
    }

    private fun observeService() {
        service.observeForever { svc ->
            svc ?: return@observeForever
            // 观察当前歌曲变化
            svc.currentSongLiveData.observeForever { song ->
                song?.let {
                    val idx = currentPlaylist.indexOfFirst { it.path == song.path }
                    if (idx != -1) currentSongIndex = idx
                }
            }
            // 观察播放状态
            svc.isPlayingLiveData.observeForever { playing ->
                isPlaying = playing
            }
            // 观察进度
            svc.progressLiveData.observeForever { (pos, dur) ->
                currentPosition = pos
                currentSongDuration = dur
            }
        }
    }

    fun init(playlist: List<SongModel>, startSong: SongModel? = null) {
        currentPlaylist = playlist

        val startIndex = startSong?.let {
            playlist.indexOfFirst { item -> item.path == it.path }.takeIf { it >= 0 }
                ?: run {
                    currentPlaylist = listOf(it) + playlist
                    0
                }
        } ?: 0

        val mode = when (playModes) {
            PlayMode.SEQUENTIAL -> LightMusicService.PlayMode.SEQUENTIAL
            PlayMode.RANDOM -> LightMusicService.PlayMode.RANDOM
            PlayMode.SINGLE_LOOP -> LightMusicService.PlayMode.SINGLE_LOOP
        }

        PlayerManager.setPlaylist(currentPlaylist, startIndex, mode)
        //Toast.makeText(,"Initialized? PlayerManager is $PlayerManager", Toast.LENGTH_SHORT).show()
    }

    fun resume() {
        PlayerManager.resume()
    }

    fun pause() {
        PlayerManager.pause()
    }

    fun togglePlayPause() {
        if (isPlaying) {
            PlayerManager.pause()
        } else {
            PlayerManager.resume()
        }
    }

    fun playNext() = PlayerManager.playNext()
    fun playPrevious() = PlayerManager.playPrevious()
    fun playSongAtIndex(index: Int) = PlayerManager.playSongAtIndex(index)

    fun seekToProgress(progress: Float) {
        if (currentSongDuration > 0) {
            seekTo((currentSongDuration * progress).toInt())
        }
    }

    fun seekTo(position: Int) = PlayerManager.seekTo(position)

    fun setPlayMode(mode: PlayMode) {
        playModes = mode
        val svcMode = when (mode) {
            PlayMode.SEQUENTIAL -> LightMusicService.PlayMode.SEQUENTIAL
            PlayMode.RANDOM -> LightMusicService.PlayMode.RANDOM
            PlayMode.SINGLE_LOOP -> LightMusicService.PlayMode.SINGLE_LOOP
        }
        PlayerManager.setPlaylist(currentPlaylist, currentSongIndex, svcMode)
    }

    fun getCurrentSong() : SongModel? {
        return PlayerManager.getCurrentSong()
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
    }
}