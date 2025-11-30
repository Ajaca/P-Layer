package com.alimanab.player

import android.widget.Toast
import androidx.compose.foundation.background
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
import kotlin.properties.Delegates
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun HomeContent() {
    val context = LocalContext.current
    var isLogin by remember { mutableStateOf(IOBundle.get(context,"login","isLogin",false) as Boolean) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        LoginCard()
        CardSongsList("My Playlist")
        /*
        LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            items(settingItems.size) { index ->
                val item = settingItems[index]
                var isTextDialog by remember { mutableStateOf(false) }
                var isPathDialog by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surface)
                        .clickable { }
                ) {
                    Text(text = item.title, fontSize = 20.sp)
                    Text(text = item.description, fontSize = 15.sp)
                    Spacer(Modifier
                        .fillMaxWidth()
                        .height(20.dp))
                }
                if (isTextDialog) {
                    TextConfigDialog(title = item.title,hint = item.hint){
                        isTextDialog = false
                    }
                }
                if (isPathDialog) {
                    PathConfigDialog(title = item.title,hint = item.hint){
                        isTextDialog = false
                    }
                }
            }
        }
    }*/
    }
}

@Composable
fun LoginCard() {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }
    var isLogin by remember {
        mutableStateOf(IOBundle.get(context,"login","isLogin",false) as Boolean)
    }
    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(refreshTrigger) {
        isLogin = IOBundle.get(context, "login", "isLogin", false) as Boolean
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(
                enabled = !isLogin,
                onClick = {showDialog = true}
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
                    refreshTrigger++
                }
            )
        }
    }
}

@Composable
fun CardSongsList(text : String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable{  },
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