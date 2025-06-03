package com.yourname.doitapp.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
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
import com.yourname.doitapp.R
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.app.PendingIntent


@Composable
fun ProfileScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val doneCount = tasks.count { it.isDone }
    val focusCount = viewModel.focusCount
    val userId = remember { UUID.randomUUID().toString().substring(0, 8) }
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                statusMessage = exportDatabaseToUri(context, uri)
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                statusMessage = importDatabaseFromUri(context, uri)
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

        Button(onClick = {
            exportLauncher.launch("backup_${System.currentTimeMillis()}.db")
        }) {
            Text("📤 選擇匯出位置")
        }

        Button(onClick = {
            importLauncher.launch(arrayOf("*/*"))
        }) {
            Text("📥 從檔案匯入")
        }

        if (statusMessage.isNotEmpty()) {
            Text(statusMessage, color = Color.Gray)
            if (statusMessage.contains("匯入成功")) {
                Text("⚠ 匯入後需重新啟動 App 才會生效", color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}

fun exportDatabaseToUri(context: Context, uri: Uri): String {
    return try {
        val dbFile = context.getDatabasePath("doit_database")
        val inputStream: InputStream = FileInputStream(dbFile)
        val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)

        if (outputStream != null) {
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            "✔ 資料庫已成功匯出"
        } else {
            "❌ 無法打開指定位置"
        }
    } catch (e: Exception) {
        "❌ 匯出錯誤：${e.message}"
    }
}

fun importDatabaseFromUri(context: Context, uri: Uri): String {
    return try {
        val dbFile = context.getDatabasePath("doit_database")
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream: OutputStream = FileOutputStream(dbFile, false)

        if (inputStream != null) {
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            "📥 匯入成功（請重新啟動 App 查看變更）"
        } else {
            "❌ 無法讀取選取的檔案"
        }
    } catch (e: Exception) {
        "❌ 匯入錯誤：${e.message}"
    }
}
fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.setExact(
            android.app.AlarmManager.RTC,
            System.currentTimeMillis() + 100,
            pendingIntent
        )
        // 關閉 App
        System.exit(0)
    }
}
