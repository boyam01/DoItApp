package com.yourname.doitapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.doitapp.data.Task


@Composable
fun DoneScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val doneTasks = tasks.filter { it.isDone }.toMutableList()
    val count = doneTasks.size
    val context = LocalContext.current
    val motivationMessage by viewModel.motivation.collectAsState()

    // 顯示激勵訊息 Toast
    motivationMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMotivation()
        }
    }

    val achievementText = when (count) {
        in 0..4 -> "🌱 初學者階段，加油！"
        in 5..9 -> "💪 累積達人等級！"
        in 10..19 -> "🔥 持續高能，效率強者！"
        else -> "🏆 任務王者！你超強！"
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("🎯 已完成任務 $count 項", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(achievementText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (doneTasks.isEmpty()) {
            Text("目前還沒有完成的任務喔，加油！")
        } else {
            LazyColumn {
                items(doneTasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(task.content, style = MaterialTheme.typography.bodyLarge)
                            if (task.subtasks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                task.subtasks.forEach {
                                    Text("• $it", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            if (!task.reminderDate.isNullOrEmpty()) {
                                Text("📅 提醒：${task.reminderDate}", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.deleteTask(task) }) {
                                Text("❌ 刪除紀錄")
                            }
                        }
                    }
                }
            }
        }
    }
}