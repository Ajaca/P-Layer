package com.alimanab.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar

@Composable
fun FetchContent() {
    Column(

    ) {
        SearchBox()
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
fun SearchBox() {
    var searchText by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        SearchBar(
            query = searchText,
            // 文本变化回调（实时更新输入状态）
            onQueryChange = { newText ->
                searchText = newText
                // 可选：输入变化时实时过滤结果（如搜索联想）
            },
            // 搜索提交回调（点击软键盘"搜索"按钮或搜索图标）
            onSearch = {
                if (it.isNotBlank()) { // 非空判断
                    println("执行搜索：$it")
                    // 实际场景：调用接口搜索、过滤本地数据等
                }
                isSearchActive = false // 提交后关闭激活状态
            },
            // 激活状态变化回调（如点击搜索框激活，点击外部关闭）
            onActiveChange = { isActive ->
                isSearchActive = isActive
                // 可选：激活时清空文本/加载历史记录
                if (isActive) {
                    searchText = "" // 激活后清空输入框
                }
            },
            // 是否激活（控制搜索框展开/收起状态）
            active = isSearchActive,
            // 前缀图标（默认搜索图标，可自定义）
            leadingIcon = {
                Text(
                    text = "Search",
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            // 占位提示文本
            placeholder = { Text("Search Something") },
            // 修饰符（控制大小、边距等）
            modifier = Modifier.fillMaxWidth()
        ) {
            // 3. 搜索结果区域（仅当 active = true 时显示）
            if (searchText.isNotBlank()) {
                // 示例：显示搜索结果列表（实际用 LazyColumn 加载大量数据）
                Text(
                    text = "Result：$searchText",
                    modifier = Modifier.padding(16.dp)
                )
            } else if (isSearchActive) {
                // 示例：激活后但未输入时，显示历史搜索记录
                Text(
                    text = "Sample",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}