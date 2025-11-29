package com.alimanab.player

import android.widget.Toast
import androidx.collection.intFloatMapOf
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import java.nio.file.WatchEvent

@Composable
fun ConfigContent() {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }
    var isLogin by remember { mutableStateOf(IOBundle.get(context,"login","isLogin",false)) }
    LaunchedEffect(refreshTrigger) {
        isLogin = IOBundle.get(context, "login", "isLogin", false) as Boolean
    }
    val settingItems = listOf(
        TextConfigElement(
            title = "Set Music path",
            description = "Set a path that music exist",
            label = 1
        )
    )
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ){
        Text(modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .height(100.dp)
            .fillMaxWidth(),
            text = "Settings",
            fontSize = 50.sp
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            items(settingItems.size) { index ->
                val item = settingItems[index]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surface)
                        .clickable { SettingClick(item.label) }
                ) {
                    Text(text = item.title, fontSize = 20.sp)
                    Text(text = item.description, fontSize = 15.sp)
                }
                if ((isLogin as Boolean)){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surface)
                            .clickable {
                                IOBundle.save(context,"login","isLogin", false)
                                refreshTrigger++
                                Toast.makeText(context,"Logout Successfully", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Text(text = "Logout", fontSize = 20.sp)
                        Text(text = "Logout current account", fontSize = 15.sp)
                    }
                }
            }
        }
    }

}

data class TextConfigElement(
    val title : String,
    val description : String,
    val label : Int
)


@Composable
fun SettingCard(Info : TextConfigElement) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable {  }
    ) {
        Text(text = Info.title, fontSize = 20.sp)
        Text(text = Info.description, fontSize = 10.sp)
    }
}

fun SettingClick(label : Int){
    when (label){
        1 -> {

        }
        else ->{}
    }
}