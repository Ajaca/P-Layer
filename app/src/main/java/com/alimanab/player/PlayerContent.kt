package com.alimanab.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

@Composable
fun MusicPlayerScreen(
    viewmodel : PlayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewmodel = viewmodel
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0f) }
    var isLoved by remember { mutableStateOf(false) }
    var isCollected by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }
    var currentSong by remember { mutableStateOf(viewmodel.getCurrentSong() as SongModel) }
    var isShowModePopUp by remember { mutableStateOf(false) }
    var playModeTag by remember { mutableStateOf(0) }
    var isShowAddToList by remember { mutableStateOf(false) }
    var isShowInfo by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(IOBundle.get(context,"login","isLogin",false) as Boolean) }
    var userName = IOBundle.get(context,"login","Username","") as String
    var UserID = sqlManager.getUserIdByUsername(userName)
    val usersLovedID = sqlManager.getPlaylistIdByName(sqlManager.getUserIdByUsername(userName),"${userName}'s Loved")
    if (isLogin && sqlManager.isSongInPlaylist(usersLovedID, currentSong.id)) {
        isLoved = true
    }

    var currentPosition by remember { mutableStateOf(viewmodel.currentPosition) }
    var currentSongDuration by remember { mutableStateOf(viewmodel.currentSongDuration) }

    LaunchedEffect(currentSong) {
        if (isLogin && sqlManager.isSongInPlaylist(usersLovedID, currentSong.id)) {
            isLoved = true
        }
    }

    //定时更新位置
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            currentPosition = viewmodel.currentPosition
            currentSongDuration = viewmodel.currentSongDuration
            currentSong = viewmodel.getCurrentSong() as SongModel
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            modifier = Modifier.height(40.dp).fillMaxWidth(),
            text = currentSong.title,
            fontSize = 30.sp
        )
        Text(
            modifier = Modifier.height(20.dp).fillMaxWidth(),
            text = currentSong.artist,
            fontSize = 15.sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (isLoved) sqlManager.removeSongFromPlaylist(sqlManager.getPlaylistIdByName(UserID,"${userName}'s Loved"),currentSong.id)
                        else sqlManager.addSongToPlaylist(sqlManager.getPlaylistIdByName(UserID,"${userName}'s Loved"),currentSong.id)
                        isLoved = !isLoved
                              },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isLoved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Fav",
                        tint = if (isLoved) Color.Red else LocalContentColor.current
                    )
                }
                Text(text = "Loved")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { isShowAddToList = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "List",
                        tint = LocalContentColor.current
                    )
                }
                Text(text = "List")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { isShowInfo = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Information",
                        tint = LocalContentColor.current
                    )
                }
                Text(text = "Info")
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Slider(
                value = currentPosition.toFloat() / currentSongDuration.toFloat(),
                onValueChange = { viewmodel.seekToProgress(it) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentPosition))
                Text(text = formatTime(currentSongDuration))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { PlayerManager.playNext() ; currentSong = viewmodel.getCurrentSong() as SongModel}) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Prev."
                    )
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying ; viewmodel.togglePlayPause()},
                    modifier = Modifier.size(64.dp)
                ) {
                    if (isPlaying) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_pause),
                            contentDescription = "Pause",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                IconButton(onClick = { PlayerManager.playNext() ; currentSong = viewmodel.getCurrentSong() as SongModel }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next",
                    )
                }
            }
            IconButton(onClick = { isShowModePopUp = true }) {
                Icon(
                    imageVector = if (playModeTag == 1) Icons.Default.Share else if (playModeTag == 2) Icons.Default.Refresh else Icons.Default.KeyboardArrowRight,
                    contentDescription = "Back"
                )
            }
        }
        if (isShowModePopUp) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = 0, y = 200), // 调整弹出位置
                onDismissRequest = { isShowModePopUp = false }
            ) {
                Surface(
                    modifier = Modifier.wrapContentWidth(unbounded = true),
                    shape = RoundedCornerShape(4.dp),
                    tonalElevation = 3.dp,
                    shadowElevation = 6.dp,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        PopupMenuItem(
                            text = "Sequential",
                            icon = Icons.Default.PlayArrow,
                            onClick = { viewmodel.setPlayMode(PlayerViewModel.PlayMode.SEQUENTIAL) ; playModeTag = 0 ;isShowModePopUp = false }
                        )
                        PopupMenuItem(
                            text = "Random",
                            icon = Icons.Default.Share,
                            onClick = { viewmodel.setPlayMode(PlayerViewModel.PlayMode.RANDOM) ; playModeTag = 1 ; isShowModePopUp = false }
                        )
                        PopupMenuItem(
                            text = "Single Loop",
                            icon = Icons.Default.Refresh,
                            onClick = { viewmodel.setPlayMode(PlayerViewModel.PlayMode.SINGLE_LOOP) ; playModeTag = 2 ; isShowModePopUp = false }
                        )
                    }
                }
            }
        }
        if (isShowInfo) {
            InfoDialog(currentSong) { isShowInfo = false }
        }
        if (isShowAddToList) {
            AddToListDialog(
                Song = currentSong,
                UserID = UserID,
                onDismiss = { isShowAddToList = false },
                onAdd = {}
            )
        }
    }
}

@Preview
@Composable
fun MusicPlayerScreenPreview() {
    Theme{
        MusicPlayerScreen(viewModel) { }
    }
}
fun formatTime(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

@Preview
@Composable
fun musicPreview(){
    Theme {
        MusicPlayerScreen(viewModel) {  }
    }
}