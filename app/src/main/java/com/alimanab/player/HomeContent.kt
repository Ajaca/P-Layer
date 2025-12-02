package com.alimanab.player

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog

@Composable
fun HomeContent( onChangeBlank : (ListModel) -> Unit ) {
    val context = LocalContext.current
    var isLogin by remember { mutableStateOf(IOBundle.get(context,"login","isLogin",false) as Boolean) }
    var refreshTrigger by remember { mutableStateOf(0) }
    sqlManager = SQLManager(context)
    var UserID = sqlManager.getUserIdByUsername(IOBundle.get(context,"login","Username","") as String)
    LaunchedEffect(refreshTrigger) {
        isLogin = IOBundle.get(context, "login", "isLogin", false) as Boolean
        if(isLogin) UserID = sqlManager.getUserIdByUsername(IOBundle.get(context,"login","Username","") as String)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        LoginCard(isLogin){ isLogin = true ; refreshTrigger++ }
        AllCardSongsList({ onChangeBlank(ListModel(name = "All Songs",owner = 0,id = -1)) })

        if (isLogin) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                val listNames = sqlManager.getUserPlaylists(UserID)
                val SongsLists = listNames.map { playlistName ->
                    val playlistId = sqlManager.getPlaylistIdByName(UserID, playlistName)
                    ListModel(
                        name = playlistName,
                        id = playlistId,
                        owner = UserID
                    )
                }
                items(SongsLists.size) { index ->
                    CardSongsList(SongsLists[index], onClick = {onChangeBlank(SongsLists[index])}, onConfig = { refreshTrigger++ })
                }

                item{
                    AddCardSongsList(){ refreshTrigger++ }
                }
            }
        }
    }
}


@Composable
fun LoginCard(isLogin : Boolean, onLogin : () -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    sqlManager = SQLManager(context)
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
                    IOBundle.save(context,"login","UserID",sqlManager.getUserIdByUsername(email))
                    showDialog = false
                    onLogin()
                }
            )
        }
    }
}


@Composable
fun CardSongsList(listModel: ListModel, onClick: () -> Unit, onConfig: () -> Unit ) {
    val context = LocalContext.current
    var isConfirmDelete by remember { mutableStateOf(false) }
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
                    .weight(3f)
                    .offset(x=20.dp),
            ) {
                Text(text = listModel.name, fontSize = 20.sp)
            }
            IconButton(modifier = Modifier.size(30.dp),onClick = { isConfirmDelete = true }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    modifier = Modifier.size(30.dp),
                    contentDescription = "Modifier",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        if (isConfirmDelete){
            DeleteConfirmDialog(listModel,
                onDismiss = { isConfirmDelete = false },
                onDeleted = { onConfig() }
            )
        }
    }
}


@Composable
fun AllCardSongsList(onClick: () -> Unit)  {
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
                Text(text = "All Songs", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun AddCardSongsList(onFinish: () -> Unit)  {
    val context = LocalContext.current
    var isDialog by remember {mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable{ isDialog = true },
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
                    imageVector = Icons.Default.Add,
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
                Text(text = "Add new List", fontSize = 20.sp)
            }
        }
        if (isDialog){
            NewListDialog( IOBundle.get(context,"login","UserID",0) as Int) { isDialog = false ; onFinish() }
        }
    }
}

@Composable
fun NewListDialog(  UserId : Int, onDismiss: () -> Unit ) {
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
                Text("Create a new List", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { sqlManager.createPlaylist(UserId,text) ; onDismiss()},
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(listModel: ListModel, onDismiss: () -> Unit, onDeleted: () -> Unit){
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
                Text("Do you sure to Delete list ${listModel.name} ?")
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
                        onClick = { sqlManager.deletePlaylistById(listModel.owner,listModel.id) ; onDeleted() ; onDismiss()},
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




@Preview
@Composable
fun HomePreview() {
    Theme{
        HomeContent(){}
    }
}