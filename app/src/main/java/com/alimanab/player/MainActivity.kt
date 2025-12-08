package com.alimanab.player

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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


lateinit var startActivityLauncher: ActivityResultLauncher<Intent>

class MainActivity : ComponentActivity() {
    private lateinit var DB: SQLManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    MaterialTheme(
        content = content
    )
}

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
    val sqlManager = remember { SQLManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var Recompose by remember { mutableIntStateOf(0) }
    var playCardTrigger by remember { mutableIntStateOf(0) }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { it != SheetValue.Expanded }
    )

    var currentSongIndex by remember { mutableStateOf(-1) }
    var currentPosition by remember { mutableStateOf(0) }
    var currentSongDuration by remember { mutableStateOf(0) }

    var SongsList by remember { mutableStateOf(emptyList<SongModel>()) }

    val viewModel = remember { PlayerViewModel() }

    val isCurrentlyPlaying by remember { derivedStateOf { viewModel.isPlaying } }
    var currentSong = viewModel.getCurrentSong()


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
        BottomNavigationItem(
            title = stringResource(R.string.fetch),
            icon = Icons.Default.Add,
            content = { FetchContent() }
        ),
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