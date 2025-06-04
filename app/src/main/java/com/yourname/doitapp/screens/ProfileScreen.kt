package com.yourname.doitapp.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val doneCount = tasks.count { it.isDone }
    val focusCount = viewModel.focusCount
    val userId = remember { UUID.randomUUID().toString().substring(0, 8) }
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    statusMessage = exportTasksAsJson(context, tasks, uri)
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    statusMessage = importTasksFromJson(context, uri, viewModel)
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "User Avatar",
            modifier = Modifier.size(96.dp),
            contentScale = ContentScale.Crop
        )

        Text("統計資訊", fontSize = 28.sp)

        Text("使用者 ID：$userId", color = Color.Gray)
        Text("完成任務數：$doneCount 項", fontSize = 20.sp)
        Text("專注次數：$focusCount 次", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            viewModel.clearAllTasks()
            viewModel.resetFocusCount()
        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("重設所有資料", color = Color.White)
        }

        Button(onClick = {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            exportLauncher.launch("DoIt_Backup_$timestamp.json")
        }) {
            Text("📤 匯出任務清單 (JSON)")
        }

        Button(onClick = {
            importLauncher.launch(arrayOf("application/json"))
        }) {
            Text("📥 匯入任務清單 (JSON)")
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(statusMessage, color = Color.Gray)
        }
    }
}

fun exportTasksAsJson(context: Context, tasks: List<Task>, uri: Uri): String {
    return try {
        val json = Gson().toJson(tasks)
        context.contentResolver.openOutputStream(uri)?.use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write(json)
                writer.flush()
            }
        }
        "✔ 匯出成功：任務清單已儲存為 JSON"
    } catch (e: Exception) {
        "❌ 匯出失敗：${e.message}"
    }
}

fun importTasksFromJson(context: Context, uri: Uri, viewModel: TaskViewModel): String {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return "❌ 無法讀取匯入檔案"
        val json = input.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Task>>() {}.type
        val tasks: List<Task> = Gson().fromJson(json, type)

        viewModel.clearAllTasks()
        tasks.forEach { viewModel.addTask(it) }

        "📥 匯入成功：已還原 ${tasks.size} 筆任務"
    } catch (e: Exception) {
        "❌ 匯入失敗：${e.message}"
    }
}
123