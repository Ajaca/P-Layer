package com.alimanab.player

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
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import kotlinx.serialization.descriptors.StructureKind
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.sp
import java.nio.file.WatchEvent

@Composable
fun ConfigContent() {
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
                        .clickable {  }
                ) {
                    Text(text = item.title, fontSize = 20.sp)
                    Text(text = item.description, fontSize = 10.sp)
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
fun SettingList(items: List<String>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        items(items) { item ->
            Text(
                text = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun SettingCard(Info : TextConfigElement) {
    Card(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .clickable(onClick = {}),
        onClick = {}

    ) {
        Column() {
            Text(
                text = Info.title
            )
            Text(
                text = Info.description
            )
        }
    }
}