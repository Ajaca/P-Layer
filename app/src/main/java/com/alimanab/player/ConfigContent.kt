package com.alimanab.player

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.intFloatMapOf
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import java.io.File
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
            hint = "Path",
            type = SettingType.path,
            todo = false
        ),
        TextConfigElement(
            title = "Import Songs from Path",
            description = "Import Songs from the Path you set",
            hint = "",
            type = SettingType.click,
            todo = true
        ),
        TextConfigElement(
            title = "Language",
            description = "Change current Language",
            hint = "Language",
            type = SettingType.list,
            todo =  false
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
                var isTextDialog by remember { mutableStateOf(false) }
                var isPathDialog by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surface)
                        .clickable {
                            when (item.type) {
                                SettingType.list -> {}
                                SettingType.text -> {
                                    isTextDialog = true
                                }

                                SettingType.switch -> {}
                                SettingType.path -> {
                                    isPathDialog = true
                                }

                                SettingType.click -> {}
                            }
                            if (item.todo){
                                ConfigTodo(item.title)
                            }
                        }
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
                    PathConfigDialog(title = item.title,hint = item.hint){ isPathDialog = false }

                }
            }
            if ((isLogin as Boolean)){
                item{
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surface)
                            .clickable {
                                IOBundle.save(context, "login", "isLogin", false)
                                refreshTrigger++
                                Toast.makeText(context, "Logout Successfully", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    ) {
                        Text(text = "Logout", fontSize = 20.sp)
                        Text(text = "Logout current account", fontSize = 15.sp)
                        Spacer(Modifier
                            .fillMaxWidth()
                            .height(20.dp))
                    }
                }
            }
        }
    }
}

data class TextConfigElement(
    val title : String,
    val description : String,
    val hint : String,
    val type : SettingType,
    val todo : Boolean
)

enum class SettingType{
    text,
    switch,
    list,
    path,
    click
}

@Composable
fun TextConfigDialog(
    title : String,
    hint : String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var text by remember {
        mutableStateOf(IOBundle.get(context, "config", title, String) as? String ?: "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(hint) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { IOBundle.save(context,"config",title,text); onDismiss() },
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
fun PathConfigDialog(
    title: String,
    hint: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var selectedPath by remember {
        mutableStateOf(
            IOBundle.get(context, "config", "musicpath",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
            ) as String
        )
    }

    // 系统文件夹选择器
    val dirPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { treeUri ->
            // 解析 Uri 为真实路径
            val path = extractPathFromTreeUri(context, treeUri)
            if (!path.isNullOrEmpty()) {
                selectedPath = path // 更新路径状态
            } else {
                Toast.makeText(context, "Can not fetch the path", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    OutlinedTextField(
                        value = selectedPath,
                        onValueChange = {},
                        label = { Text(hint) },
                        modifier = Modifier.fillMaxSize(),
                        readOnly = true,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { dirPickerLauncher.launch(null) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        IOBundle.save(context, "config", "musicpath", selectedPath)
                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}


private fun extractPathFromTreeUri(context: Context, treeUri: android.net.Uri): String? {
    return try {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val parts = docId.split(":", limit = 2)
        val (root, subPath) = if (parts.size == 1) parts[0] to "" else parts[0] to parts[1]

        // 解析根路径
        val basePath = when (root) {
            "primary" -> Environment.getExternalStorageDirectory().absolutePath
            else -> "/storage/$root"
        }

        // 拼接子路径（处理斜杠重复）
        if (subPath.isEmpty()) basePath else "$basePath/$subPath".replace("//", "/")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun ConfigTodo(title : String){
    when (title){
        "Import Songs from Path" -> {
        }
        else -> {}
    }
}