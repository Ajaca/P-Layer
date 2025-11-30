package com.alimanab.player

import com.alimanab.player.SQL
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.DialogProperties

class MainActivity : ComponentActivity() {
    private lateinit var DB: SQLManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DB = SQLManager(this)
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
    var isPlayed by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var isShowPlay by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true, // 跳过部分展开状态，直接全屏
        confirmValueChange = { it != androidx.compose.material3.SheetValue.PartiallyExpanded }
    )
    val navItems = listOf(
        BottomNavigationItem(
            title = stringResource(R.string.fetch),
            icon = Icons.Default.Add,
            content = { FetchContent() }
        ),
        BottomNavigationItem(
            title = stringResource(R.string.home),
            icon = Icons.Default.Home,
            content = { HomeContent() }
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
                visible = !isShowPlay,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ){
                Column(){
                    if (isPlayed) {
                        NowPlayingCard(
                            isplay = isPlaying,
                            onPlayClick = {
                                //TODO:
                            },
                            onCardClick = {
                                isShowPlay = true
                            }
                        )
                    }
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
            navItems[selectedItem].content()
        }
    }
    if (isShowPlay) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = {
                // 点击遮罩或返回键，关闭播放页面（退栈）
                isShowPlay = false
            },
            // 关键：设置全屏样式，去掉默认圆角
            modifier = Modifier.fillMaxSize(),
            scrimColor = Color.Black.copy(alpha = 0.5f),
        ) {
            // 播放页面内容（全屏）
            MusicPlayerScreen(
                onBack = {
                    // 点击播放页面的返回按钮，关闭页面
                    isShowPlay = false
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