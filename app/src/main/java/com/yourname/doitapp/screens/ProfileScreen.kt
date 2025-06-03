package com.yourname.doitapp.screens

import android.content.Context
import android.os.Environment
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
import com.yourname.doitapp.R
import com.yourname.doitapp.screens.TaskViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val doneCount = tasks.count { it.isDone }
    val focusCount = viewModel.focusCount
    val userId = remember { UUID.randomUUID().toString().substring(0, 8) }
    val context = LocalContext.current
    var exportStatus by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 加上虛擬頭像
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(96.dp)
                .padding(8.dp),
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            exportStatus = exportDatabase(context)
        }) {
            Text("📦 匯出資料庫")
        }

        if (exportStatus.isNotEmpty()) {
            Text(exportStatus, color = Color.Gray)
        }
    }
}

fun exportDatabase(context: Context): String {
    return try {
        val dbFile = context.getDatabasePath("doit_database")
        val backupDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "DoItBackup"
        )
        if (!backupDir.exists()) backupDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFile = File(backupDir, "backup_$timestamp.db")

        val srcChannel: FileChannel = FileInputStream(dbFile).channel
        val dstChannel: FileChannel = FileOutputStream(backupFile).channel
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size())
        srcChannel.close()
        dstChannel.close()

        "✔ 已匯出：${backupFile.absolutePath}"
    } catch (e: Exception) {
        "❌ 匯出失敗：${e.message}"
    }
}