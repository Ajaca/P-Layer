package com.alimanab.player

import android.annotation.SuppressLint
import android.icu.text.TimeZoneFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsListContent( list : ListModel, onBackClick: () -> Unit = {}, onPlay: (List<SongModel>,SongModel) -> Unit) {
    val context = LocalContext.current
    var isPlay by remember { mutableStateOf(false) }
    var isPlayed by remember { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val sqlManager = remember { SQLManager(context = context) }
    val SongsInList by remember(list) {
        derivedStateOf {
            if (list.id == -1) {
                sqlManager.getAllSongs()
            } else {
                sqlManager.getPlaylistSongs(list.id)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(
                            text = list.name,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${SongsInList.size} Songs",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                scrollBehavior = scrollBehavior
            )
        },
        content = { innerPadding ->
            if (SongsInList.isEmpty()) {
                EmptyPlaylistContent(
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(SongsInList.size) { index ->
                        SongCard(
                            Song = SongsInList[index],
                            List = list,
                            onConfig = {},
                            onClick = {
                                onPlay(SongsInList,SongsInList[index])
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun EmptyPlaylistContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_note_foreground),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Empty",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add Songs to this List",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SongCard(Song : SongModel,List : ListModel,onClick: () -> Unit, onConfig: () -> Unit ) {
    val context = LocalContext.current
    var isConfigVisible by remember { mutableStateOf(false) }
    var isShowAddToList by remember { mutableStateOf(false) }
    var isShowRemoveFromList by remember { mutableStateOf(false) }
    var isShowInfo by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable{ onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = RectangleShape),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_note_foreground),
                    contentDescription = "playlist",
                    modifier = Modifier
                        .size(16.dp)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier
                    .weight(3f)
                    .offset(x=20.dp),
            ) {
                Text(text = Song.title, fontSize = 20.sp)
            }
            IconButton(modifier = Modifier.size(30.dp),onClick = { isConfigVisible = true }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    modifier = Modifier.size(30.dp),
                    contentDescription = "Modifier",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        if (isConfigVisible) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = -16, y = 150), // 调整弹出位置
                onDismissRequest = { isConfigVisible = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    tonalElevation = 3.dp,
                    shadowElevation = 6.dp,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        PopupMenuItem(
                            text = "Love",
                            icon = Icons.Default.Favorite,
                            onClick = { isConfigVisible = false }
                        )
                        PopupMenuItem(
                            text = "Add to List",
                            icon = Icons.Default.Add,
                            onClick = { isShowAddToList = true ; isConfigVisible = false }
                        )
                        if(List.id != -1){
                            PopupMenuItem(
                                text = "Remove from List",
                                icon = Icons.Default.Delete,
                                onClick = { isShowRemoveFromList = true ; isConfigVisible = false }
                            )
                        }
                        HorizontalDivider()
                        PopupMenuItem(
                            text = "Info",
                            icon = Icons.Default.Info,
                            onClick = { isShowInfo = true ; isConfigVisible = false }
                        )
                    }
                }
            }
        }
        if (isShowInfo) {
            InfoDialog(Song) { isShowInfo = false }
        }
        if (isShowAddToList) {
            AddToListDialog(
                Song = Song,
                UserID = sqlManager.getUserIdByUsername(IOBundle.get(context,"login","Username","") as String),
                onDismiss = { isShowAddToList = false },
                onAdd = {}
            )
        }
        if (isShowRemoveFromList) {
            RemoveConfirmDialog(List,Song) { isShowRemoveFromList = false }
        }
    }
}

@Composable
fun InfoDialog(Song : SongModel,onDismiss : () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card (
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(Modifier.padding(6.dp)) {
                Text("Information", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(10.dp))
                Text("Title: ${Song.title}", style = MaterialTheme.typography.bodyMedium)
                Text("Artist: ${Song.artist}", style = MaterialTheme.typography.bodyMedium)
                Text("Duration: " + formatTime(Song.duration), style = MaterialTheme.typography.bodyMedium)
                Text("Path: ${Song.path}", style = MaterialTheme.typography.bodyMedium)
                Text("Filename: ${Song.fileName}", style = MaterialTheme.typography.bodyMedium)
                Text("Size: ${Song.size}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(20.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = onDismiss) {
                    Text("OK", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun AddToListDialog(UserID : Int,Song: SongModel,onDismiss : () -> Unit,onAdd : (Int) -> Unit) {
    val List = sqlManager.getUserPlaylists(UserID)
    val ListModels = List.map { playlistName ->
        val playlistId = sqlManager.getPlaylistIdByName(UserID, playlistName)
        ListModel(
            name = playlistName,
            id = playlistId,
            owner = UserID
        )
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth().padding(20.dp)) {
            Column(Modifier.padding(10.dp)) {
                Text("Add to List",style = MaterialTheme.typography.headlineMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    items(List.size) { index ->
                        CardSongsList(
                            listModel = ListModels[index],
                            onClick = { sqlManager.addSongToPlaylist(ListModels[index].id,Song.id) ; onDismiss() },
                            onConfig = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RemoveConfirmDialog(list: ListModel,Song: SongModel, onDismiss: () -> Unit){
    val context = LocalContext.current
    var text by remember { mutableStateOf( "" )}
    sqlManager = SQLManager(context)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Warning", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Do you sure to Delete ${Song.title} from ${list.name} ?")
                Spacer(modifier = Modifier.height(16.dp))
                Row(){
                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .weight(2f)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(20.dp))
                    Button(
                        onClick = { sqlManager.removeSongFromPlaylist(list.id,Song.id) ; onDismiss()},
                        modifier = Modifier
                            .weight(2f)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun PopupMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
