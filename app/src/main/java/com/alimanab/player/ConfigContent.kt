package com.alimanab.player

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import java.io.File

@Composable
fun ConfigContent() {
    val context = LocalContext.current

    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) {
            Toast.makeText(context, "存储权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "需要存储权限才能使用完整功能", Toast.LENGTH_SHORT).show()
        }
    }

    // 初始化权限检查
    LaunchedEffect(Unit) {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        permissionGranted = granted

        // 如果没有权限，自动申请
        if (!granted) {
            permissionLauncher.launch(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_AUDIO
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }


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
                                ConfigTodo(context,item.title)
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
            val path = extractPathFromTreeUri(context, treeUri)
            if (!path.isNullOrEmpty()) {
                selectedPath = path
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


fun ConfigTodo(context: Context, title: String) {
    when (title) {
        "Import Songs from Path" -> {
            importSongsFromPath(context)
        }
        else -> {}
    }
}

private fun importSongsFromPath(context: Context) {
    try {
        // 使用 IOBundle 读取配置的路径
        val musicPathObj = IOBundle.get(context, "config", "musicpath", "")

        // 检查是否返回了 StringCompanionObject
        val musicPath = if (musicPathObj is String) {
            musicPathObj
        } else {
            val pathStr = musicPathObj?.toString() ?: ""
            if (pathStr == "kotlin.jvm.internal.StringCompanionObject" || pathStr.isEmpty()) {
                Toast.makeText(context, "请先设置音乐路径", Toast.LENGTH_SHORT).show()
                return
            }
            pathStr
        }

        if (musicPath.isEmpty()) {
            Toast.makeText(context, "请先设置音乐路径", Toast.LENGTH_SHORT).show()
            return
        }

        val musicDir = File(musicPath)
        if (!musicDir.exists() || !musicDir.isDirectory) {
            Toast.makeText(context, "指定的路径不存在或不是目录: $musicPath", Toast.LENGTH_SHORT).show()
            return
        }

        // 使用与 Config() 相同的递归扫描逻辑
        val audioFiles = mutableListOf<File>()
        traverseDirForImport(musicDir, audioFiles)

        if (audioFiles.isEmpty()) {
            Toast.makeText(context, "在指定路径下未找到音频文件", Toast.LENGTH_SHORT).show()
            return
        }

        // 使用 SQL 方法导入数据库
        val sqlHelper = SQLManager(context)
        var newInsertCount = 0
        var alreadyExistsCount = 0
        var failedCount = 0

        // 预先检查哪些歌曲已存在（优化性能）
        val existingPaths = mutableSetOf<String>()
        audioFiles.forEach { file ->
            val song = createSongFromFile(file)
            song?.let {
                if (sqlHelper.isSongExists(it.path)) {
                    existingPaths.add(it.path)
                }
            }
        }

        // 导入歌曲
        audioFiles.forEach { file ->
            val song = createSongFromFile(file)
            if (song != null) {
                if (existingPaths.contains(song.path)) {
                    // 歌曲已存在（从预检查结果中获取）
                    alreadyExistsCount++
                } else {
                    // 尝试插入新歌曲
                    val result = sqlHelper.insertSong(song)
                    if (result != -1L) {
                        newInsertCount++
                    } else {
                        // 插入失败（可能是并发问题或其他错误）
                        failedCount++
                    }
                }
            } else {
                failedCount++
            }
        }

        // 构建准确的提示信息
        val message = when {
            newInsertCount > 0 && alreadyExistsCount > 0 ->
                "成功导入 $newInsertCount 首新歌曲，$alreadyExistsCount 首已存在"
            newInsertCount > 0 ->
                "成功导入全部 $newInsertCount 首歌曲"
            alreadyExistsCount > 0 && failedCount == 0 ->
                "所有 ${alreadyExistsCount} 首歌曲均已存在"
            failedCount > 0 ->
                "导入完成：$newInsertCount 首新导入，$alreadyExistsCount 首已存在，$failedCount 首失败"
            else ->
                "导入完成，共处理 ${audioFiles.size} 首歌曲"
        }

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 与 Config() 中相同的目录遍历函数 - 重命名避免冲突
 */
private fun traverseDirForImport(dir: File, result: MutableList<File>) {
    dir.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            traverseDirForImport(file, result) // 递归
        } else if (file.isFile && isAudioFile(file.name)) {
            result.add(file)
        }
    }
}

/**
 * 与 Config() 中完全相同的音频文件判断函数
 */
private fun isAudioFile(name: String): Boolean {
    val n = name.lowercase()
    return n.endsWith(".mp3") || n.endsWith(".flac") || n.endsWith(".wav") ||
            n.endsWith(".m4a") || n.endsWith(".aac") || n.endsWith(".ogg") ||
            n.endsWith(".wma") || n.endsWith(".ape")
}

/**
 * 从 TreeUri 提取路径 - 与 Config() 中完全相同
 */
private fun extractPathFromTreeUri(treeUri: Uri): String? {
    return try {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val parts = docId.split(":", limit = 2)
        val (root, subPath) = if (parts.size == 1) parts[0] to "" else parts[0] to parts[1]

        val basePath = when (root) {
            "primary" -> Environment.getExternalStorageDirectory().absolutePath
            else -> "/storage/$root"
        }

        if (subPath.isEmpty()) {
            basePath
        } else {
            "$basePath/$subPath".replace("//", "/")
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * 创建 SongModel 对象 - 根据 SQL 需求调整
 */
private fun createSongFromFile(file: File): SongModel? {
    return try {
        // 使用 MediaMetadataRetriever 提取元数据（与 Config() 中的扫描逻辑配合）
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.absolutePath)

        val title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            ?: file.nameWithoutExtension

        val artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            ?: "未知艺术家"

        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = durationStr?.toIntOrNull() ?: 0

        mediaMetadataRetriever.release()

        SongModel(
            title = title,
            artist = artist,
            duration = duration,
            path = file.absolutePath,
            fileName = file.name,
            size = file.length()
        )
    } catch (e: Exception) {
        println("无法读取音频元数据: ${file.absolutePath}, ${e.message}")
        // 即使无法读取元数据，也创建一个基本的 SongModel
        SongModel(
            title = file.nameWithoutExtension,
            artist = "未知艺术家",
            duration = 0,
            path = file.absolutePath,
            fileName = file.name,
            size = file.length()
        )
    }
}