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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape


@Composable
fun TaskListScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("任務清單", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { showDialog = true }) {
                Text("新增任務")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        tasks.forEach { task ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.elevatedCardElevation(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = { viewModel.toggleTaskDone(task) }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.content,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (task.subtasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            task.subtasks.forEach { sub ->
                                Text(
                                    text = "‧ $sub",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }

                        if (!task.reminderDate.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFFF1F8E9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = "提醒：${task.reminderDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
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
                        Task(content = title, subtasks = subtasks, reminderDate = date)
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
            Button(
                onClick = {
                    if (title.isNotBlank()) onSave(title, subtasks, reminderDate)
                }
            ) { Text("儲存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任務標題") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                    Text(
                        text = "‧ $it",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
