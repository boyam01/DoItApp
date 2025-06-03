package com.yourname.doitapp.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.doitapp.data.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskListScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("任務清單", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { showDialog = true }) {
            Text("新增任務")
        }

        Spacer(modifier = Modifier.height(16.dp))

        tasks.forEach { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = { viewModel.toggleTaskDone(task) }
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.content,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        task.subtasks.forEach { sub ->
                            Text("• $sub", style = MaterialTheme.typography.bodySmall)
                        }
                        if (!task.reminderDate.isNullOrEmpty()) {
                            Text(
                                "提醒日期：${task.reminderDate}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddTaskDialog(
                onDismiss = { showDialog = false },
                onSave = { title, subtasks, date ->
                    viewModel.addTask(
                        Task(
                            content = title,
                            reminderDate = date,
                            subtasks = subtasks
                        )
                    )
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onSave: (String, List<String>, String?) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var subtaskInput by remember { mutableStateOf("") }
    var subtasks by remember { mutableStateOf(listOf<String>()) }
    var reminderDate by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增任務") },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) onSave(title, subtasks, reminderDate)
            }) { Text("儲存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任務標題") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        label = { Text("新增子任務") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (subtaskInput.isNotBlank()) {
                            subtasks = subtasks + subtaskInput
                            subtaskInput = ""
                        }
                    }) {
                        Text("+")
                    }
                }

                subtasks.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selected = Calendar.getInstance()
                            selected.set(year, month, dayOfMonth)
                            reminderDate = dateFormat.format(selected.time)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(reminderDate ?: "設定提醒日期")
                }
            }
        }
    )
}