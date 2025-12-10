package com.alimanab.player

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay


lateinit var sqlManager: SQLManager

class MainActivity : ComponentActivity() {
    private lateinit var DB: SQLManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sqlManager = SQLManager(this)
        DB = SQLManager(this)
        PlayerManager.init(this)
        PlayerManager.bind()
        setContent {
            Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()

                }
            }
        }
    }
}

@Composable
fun Theme(content: @Composable () -> Unit) {
    val themeValue = IOBundle.get(LocalContext.current,"config","Set Colour",0)

    val colorScheme = when (themeValue) {
        0 -> lightColorScheme(
            primary = Color(0xFF6750A4),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEADDFF),
            onPrimaryContainer = Color(0xFF21005D),

            secondary = Color(0xFF625B71),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8DEF8),
            onSecondaryContainer = Color(0xFF1D192B),

            tertiary = Color(0xFF7D5260),
            background = Color(0xFFFFFBFE),
            onBackground = Color(0xFF1C1B1F),

            surface = Color(0xFFFFFBFE),
            onSurface = Color(0xFF1C1B1F),

            error = Color(0xFFB3261E),
            onError = Color.White,
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        )

        1 -> darkColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF4F378B),
            onPrimaryContainer = Color(0xFFEADDFF),

            secondary = Color(0xFFCCC2DC),
            onSecondary = Color(0xFF332D41),
            secondaryContainer = Color(0xFF4A4458),
            onSecondaryContainer = Color(0xFFE8DEF8),

            tertiary = Color(0xFFEFB8C8),
            background = Color(0xFF1C1B1F),
            onBackground = Color(0xFFE6E1E5),

            surface = Color(0xFF1C1B1F),
            onSurface = Color(0xFFE6E1E5),

            error = Color(0xFFF2B8B5),
            onError = Color(0xFF601410),
            errorContainer = Color(0xFF8C1D18),
            onErrorContainer = Color(0xFFF9DEDC)
        )

        2 -> lightColorScheme(
            primary = Color(0xFF1976D2),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD1E4FF),
            onPrimaryContainer = Color(0xFF001D36),

            secondary = Color(0xFF1565C0),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFB7E3FF),
            onSecondaryContainer = Color(0xFF001F33),

            tertiary = Color(0xFF006A6A),
            background = Color(0xFFE3F2FD),
            onBackground = Color(0xFF1A1C1E),

            surface = Color.White,
            onSurface = Color(0xFF1A1C1E),

            error = Color(0xFFBA1A1A),
            onError = Color.White,
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002)
        )

        3 -> lightColorScheme(
            primary = Color(0xFF2E7D32),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFB7FACD),
            onPrimaryContainer = Color(0xFF002109),

            secondary = Color(0xFF1B5E20),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFA5D6A7),
            onSecondaryContainer = Color(0xFF00390A),

            tertiary = Color(0xFF386641),
            background = Color(0xFFE8F5E8),
            onBackground = Color(0xFF1B5E20),

            surface = Color.White,
            onSurface = Color(0xFF1B5E20),

            error = Color(0xFFD32F2F),
            onError = Color.White,
            errorContainer = Color(0xFFFFEBEE),
            onErrorContainer = Color(0xFF410E0B)
        )

        else -> lightColorScheme() // 默认
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

lateinit var viewModel : PlayerViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var selectedItem by rememberSaveable {mutableStateOf(0)}
    var isShowPlayCard by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isShowPlaySheet by remember { mutableStateOf(false) }
    var isOpenSongList by remember { mutableStateOf(false)}
    var SongsListStatus by remember { mutableStateOf<ListModel?>(null) }

    sqlManager = SQLManager(context)
    if (sqlManager.getUserIdByUsername("GLOBAL") == -1) {
        sqlManager.register("GLOBAL","\t")
        sqlManager.createPlaylist(sqlManager.getUserIdByUsername("GLOBAL"),"History")
    }
    val GLOBAL_ID = sqlManager.getUserIdByUsername("GLOBAL")
    val HISTORY_ID =sqlManager.getPlaylistIdByName(GLOBAL_ID,"History")

    var currentSong: SongModel
    var previousSong = SongModel(-1,"","",0,"","",0)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentSong = viewModel.getCurrentSong() ?: SongModel(-1,"","",0,"","",0)
            println("Current Song is ${currentSong.title}")
            if (previousSong != currentSong && currentSong.id != -1) {
                if (sqlManager.isSongInPlaylist(GLOBAL_ID,currentSong.id)) {
                    sqlManager.removeSongFromPlaylist(HISTORY_ID,currentSong.id)
                    sqlManager.addSongToPlaylist(HISTORY_ID, currentSong.id)
                } else {
                    sqlManager.addSongToPlaylist(HISTORY_ID, currentSong.id)
                }
            }
            previousSong = currentSong
        }
    }
    val sqlManager = remember { SQLManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var Recompose by remember { mutableIntStateOf(0) }
    var playCardTrigger by remember { mutableIntStateOf(0) }


    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { it != SheetValue.Expanded }
    )
    var SongsList by remember { mutableStateOf(emptyList<SongModel>()) }

    viewModel = remember { PlayerViewModel() }

    val isCurrentlyPlaying by remember { derivedStateOf { viewModel.isPlaying } }


    var showPlaycard by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(showPlaycard) {
        showPlaycard?.let {
            if (!isShowPlayCard) {
                isShowPlayCard = true
            }
            showPlaycard = null
        }
    }

    LaunchedEffect(isCurrentlyPlaying) { isPlaying = isCurrentlyPlaying }

    val navItems = listOf(
        /*
        BottomNavigationItem(
            title = stringResource(R.string.fetch),
            icon = Icons.Default.Add,
            content = { FetchContent() }
        ),
        */
        BottomNavigationItem(
            title = stringResource(R.string.home),
            icon = Icons.Default.Home,
            content = {
                HomeContent(){
                    list -> SongsListStatus = list
                    SongsList = sqlManager.getPlaylistSongs(list.id)
                    isOpenSongList = true
                }
            }
        ),
        BottomNavigationItem(
            title = stringResource(R.string.config),
            icon = Icons.Default.Build,
            content = { ConfigContent() }
        )
    )
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = !isShowPlaySheet,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ){
                Column(){
                    if (isShowPlayCard) {
                        key(playCardTrigger) {
                            NowPlayingCard(
                                isPlaying = isPlaying,
                                Song = viewModel.getCurrentSong() as SongModel,
                                onPlayClick = {
                                    viewModel.togglePlayPause()
                                },
                                onCardClick = {
                                    isShowPlaySheet = true
                                },
                                onNextClick = {
                                    viewModel.playNext()
                                    playCardTrigger++
                                }
                            )
                        }
                    }
                    if (!isOpenSongList){
                        BottomAppBar {
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedItem == index,
                                    onClick = { selectedItem = index },
                                    icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                                    label = { Text(text = item.title) }
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            if (isOpenSongList) {
                SongsListContent(
                    SongsListStatus as ListModel,
                    onBackClick = { isOpenSongList = false },
                    onPlay = { list, song -> //list = List<SongModel>, song = SongModel
                        showPlaycard = true
                        viewModel.init(list, song)
                        viewModel.togglePlayPause()
                    }
                )
            } else {
                navItems[selectedItem].content()
            }
        }
    }
    if (isShowPlaySheet) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { isShowPlaySheet = false },
            modifier = Modifier.fillMaxSize().wrapContentHeight(),
            scrimColor = Color.Black.copy(alpha = 0.5f),
        ) {
            MusicPlayerScreen(
                viewmodel = viewModel,
                onBack = {
                    isShowPlaySheet = false
                }
            )
        }
    }
}

data class BottomNavigationItem(
    val title : String,
    val icon : ImageVector,
    val content : @Composable () -> Unit
)


@Preview(showBackground = true)
@Composable
fun Preview() {
    Theme {
        MainScreen()
    }
}