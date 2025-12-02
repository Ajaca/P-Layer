package com.alimanab.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MusicPlayerScreen(
    onBack: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0f) }
    var isLiked by remember { mutableStateOf(false) }
    var isCollected by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            modifier = Modifier.height(40.dp).fillMaxWidth(),
            text = "Song Sample",
            fontSize = 30.sp
        )
        Text(
            modifier = Modifier.height(20.dp).fillMaxWidth(),
            text = "Song Sample",
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
                    onClick = { isLiked = !isLiked },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Fav",
                        tint = if (isLiked) Color.Red else LocalContentColor.current
                    )
                }
                Text(text = "Fav")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { isCollected = !isCollected },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isCollected) Icons.Filled.Build else Icons.Filled.Done,
                        contentDescription = "List",
                        tint = if (isCollected) Color.Blue else LocalContentColor.current
                    )
                }
                Text(text = "List")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { isDownloaded = !isDownloaded },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isDownloaded) Icons.Filled.Done else Icons.Filled.Add,
                        contentDescription = "Download",
                        tint = if (isDownloaded) Color.Green else LocalContentColor.current
                    )
                }
                Text(text = "Down")
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Slider(
                value = currentProgress,
                onValueChange = { currentProgress = it },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime((currentProgress * 180).toInt())) // 假设总时长180秒
                Text(text = formatTime(180)) // 总时长
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
                    contentDescription = "返回"
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* 上一首 */ }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "上一首"
                    )
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Close else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { /* 下一首 */ }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "下一首"
                    )
                }
            }

            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Preview
@Composable
fun MusicPlayerScreenPreview() {
    Theme{
        MusicPlayerScreen { }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

@Preview
@Composable
fun musicPreview(){
    Theme {
        MusicPlayerScreen {  }
    }
}