package com.yourname.doitapp.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourname.doitapp.R
import com.yourname.doitapp.data.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

// --- 統一的應用程式配色方案 ---
private val PrimaryPurple = Color(0xFF6C5CE7)
private val SoftWhite = Color(0xFFFDFCFF)
private val WarmGray = Color(0xFFF8F9FA)
private val DarkText = Color(0xFF2D3436)
private val DangerRed = Color(0xFFE17055)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val doneCount = tasks.count { it.isDone }
    val focusCount = viewModel.focusCount
    val userId = remember { "ID: " + UUID.randomUUID().toString().substring(0, 8).uppercase() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val message = exportTasksAsJson(context, tasks, uri)
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val message = importTasksFromJson(context, uri, viewModel)
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(SoftWhite, WarmGray)))
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = "個人檔案", tint = PrimaryPurple)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("個人檔案", fontWeight = FontWeight.Bold, color = DarkText, fontSize = 20.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 頭像與ID ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.tomato), // 使用番茄圖示作為頭像
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, PrimaryPurple, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(userId, color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- 統計資訊卡片 ---
                item {
                    ProfileCard(
                        title = "統計資訊",
                        icon = Icons.Filled.QueryStats
                    ) {
                        StatRow(icon = Icons.Outlined.TaskAlt, label = "完成任務數", value = "$doneCount 項")
                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = WarmGray)
                        StatRow(icon = Icons.Outlined.StarOutline, label = "專注次數", value = "$focusCount 次")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- 資料管理卡片 ---
                item {
                    ProfileCard(title = "資料管理", icon = Icons.Filled.Folder) {
                        Button(
                            onClick = {
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                exportLauncher.launch("DoIt_Backup_$timestamp.json")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Icon(Icons.Filled.Upload, contentDescription = "匯出")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("匯出任務清單 (JSON)")
                        }

                        Button(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = "匯入")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("匯入任務清單 (JSON)")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- 危險區域卡片 ---
                item {
                    ProfileCard(title = "危險區域", icon = Icons.Filled.Warning, titleColor = DangerRed) {
                        Button(
                            onClick = {
                                viewModel.clearAllTasks()
                                viewModel.resetFocusCount()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("所有資料已重設")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = "重設")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("重設所有資料", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// 為了重用性，將卡片樣式提取為一個共用元件
@Composable
private fun ProfileCard(
    title: String,
    icon: ImageVector,
    titleColor: Color = PrimaryPurple,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = title, tint = titleColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = DarkText, fontSize = 16.sp)
            }
            Divider(color = WarmGray)
            content()
        }
    }
}

// 統計數據行的共用元件
@Composable
private fun StatRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = PrimaryPurple)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, color = DarkText, fontSize = 16.sp)
        }
        Text(value, color = DarkText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

// 檔案操作函式 (保持不變)
fun exportTasksAsJson(context: Context, tasks: List<Task>, uri: Uri): String {
    return try {
        val json = Gson().toJson(tasks)
        context.contentResolver.openOutputStream(uri)?.use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write(json)
            }
        }
        "✔ 匯出成功！"
    } catch (e: Exception) {
        "❌ 匯出失敗：${e.message}"
    }
}

fun importTasksFromJson(context: Context, uri: Uri, viewModel: TaskViewModel): String {
    return try {
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
        val type = object : TypeToken<List<Task>>() {}.type
        val tasks: List<Task> = Gson().fromJson(json, type)

        viewModel.clearAllTasks()
        tasks.forEach { viewModel.addTask(it) }

        "📥 匯入成功：已還原 ${tasks.size} 筆任務"
    } catch (e: Exception) {
        "❌ 匯入失敗：${e.message}"
    }
}