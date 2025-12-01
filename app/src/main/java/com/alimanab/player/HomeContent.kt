package com.alimanab.player

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import kotlin.jvm.java

@Composable
fun HomeContent() {
    val context = LocalContext.current
    var isLogin by remember { mutableStateOf(IOBundle.get(context,"login","isLogin",false) as Boolean) }
    var refreshTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(refreshTrigger) { isLogin = IOBundle.get(context, "login", "isLogin", false) as Boolean }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        LoginCard(isLogin){ isLogin = true ; refreshTrigger++ }
        CardSongsList("All Songs"){
            val intent = Intent(context, SongsListActivity::class.java).apply {
                putExtra("song_list_id", -1)
                putExtra("list_name", "All Songs")
            }
            startActivityLauncher.launch(intent)
        }

        if (isLogin) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                val SongsList = listOf(
                    SongsListElement(
                        name = "Sample SongList 1",
                        id = 0,
                        owner = 0
                    ),
                    SongsListElement(
                        name = "Sample SongList 2",
                        id = 0,
                        owner = 0
                    )
                )
                items(SongsList.size) { index ->
                    CardSongsList(SongsList[index].name){}
                }

                item{
                    CardSongsList("Add new List"){}
                }
            }
        }
    }
}

data class SongsListElement(
    val name : String,
    val id : Int,
    val owner : Int
)

@Composable
fun LoginCard(isLogin : Boolean, onLogin : () -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(
                enabled = !isLogin,
                onClick = { showDialog = true }
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "login",
                        modifier = Modifier
                            .size(16.dp)
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x=20.dp),
            ) {
                when (isLogin as Boolean){
                    true ->Text(
                        text = IOBundle.get(context,"login","Username","") as String,
                        fontSize = 20.sp
                    )
                    false ->Text(
                        text = stringResource(R.string.click_to_login),
                        fontSize = 20.sp
                    )

                }
            }
        }

        if (showDialog) {
            LoginDialog(
                onDismiss = {
                    showDialog = false
                },
                onRegister = { email,password ->
                    showDialog = false
                },
                onLogin = { email,password ->
                    IOBundle.save(context,"login","isLogin", true)
                    IOBundle.save(context,"login","Username",email)
                    showDialog = false
                    onLogin()
                }
            )
        }
    }
}

@Composable
fun CardSongsList(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable{ onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = RectangleShape),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "playlist",
                    modifier = Modifier
                        .size(16.dp)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x=20.dp),
            ) {
                Text(text = text, fontSize = 20.sp)
            }
        }
    }
}

@Preview
@Composable
fun HomePreview() {
    Theme{
        HomeContent()
    }
}