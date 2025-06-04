package com.yourname.doitapp.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.doitapp.data.Task
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.app.TimePickerDialog


private val LightSandYellow = Color(0xFF000000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSandYellow)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "我的任務清單",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightSandYellow)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Color(0xFF9C6EF5),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "新增任務")
                }
            },
            content = { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "今天要做什麼？",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF0C0C07),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (tasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = "目前還沒有任務，點擊 + 來新增",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        items(tasks) { task ->
                            var expanded by remember { mutableStateOf(false) }
                            val taskId = task.id

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = task.isDone,
                                            onCheckedChange = { viewModel.toggleTaskDone(task) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF3F51B5),
                                                uncheckedColor = Color.Gray
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = task.content,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = if (task.isDone) Color.Gray else Color.Black,
                                                fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Medium
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { viewModel.deleteTask(task) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "刪除任務",
                                                tint = Color(0xFFF34F41)
                                            )
                                        }
                                    }

                                    if (task.subtasks.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(
                                            onClick = { expanded = !expanded },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SubdirectoryArrowRight,
                                                contentDescription = null,
                                                tint = Color(0xFF000000)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (expanded) "隱藏子任務" else "查看子任務",
                                                color = Color(0xFF000000)
                                            )
                                        }
                                        if (expanded) {
                                            Column(modifier = Modifier.padding(start = 24.dp)) {
                                                task.subtasks.forEachIndexed { index, subtask ->
                                                    val checked = task.subtaskStates.getOrNull(index) == true
                                                    var editedText by remember { mutableStateOf(subtask) }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Checkbox(
                                                            checked = checked,
                                                            onCheckedChange = {
                                                                viewModel.updateSubtaskState(taskId, index, !checked)
                                                            }
                                                        )
                                                        OutlinedTextField(
                                                            value = editedText,
                                                            onValueChange = {
                                                                editedText = it
                                                                viewModel.updateSubtaskContent(taskId, index, it)
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            textStyle = MaterialTheme.typography.bodySmall
                                                        )
                                                        IconButton(onClick = {
                                                            viewModel.removeSubtask(taskId, index)
                                                        }) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "刪除子任務",
                                                                tint = Color(0xFFD32F2F)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    task.reminderDate?.let { dateStr ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        val targetDate = try { sdf.parse(dateStr) } catch (e: Exception) { null }
                                        val daysLeft = targetDate?.let {
                                            val diff = it.time - System.currentTimeMillis()
                                            TimeUnit.MILLISECONDS.toDays(diff).toInt()
                                        }
                                        val chipText = when {
                                            daysLeft == null -> "❓ 未設定"
                                            daysLeft > 0 -> "⏳ 還有 $daysLeft 天"
                                            daysLeft == 0 -> "⚠️ 今天到期"
                                            else -> "❌ 已過期"
                                        }
                                        val chipColor = when {
                                            daysLeft == null -> Color.LightGray
                                            daysLeft > 0 -> Color.Gray
                                            daysLeft == 0 -> Color(0xFFFFA000)
                                            else -> Color(0xFFD32F2F)
                                        }
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(chipText, color = Color.White) },
                                            colors = AssistChipDefaults.assistChipColors(containerColor = chipColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )

        if (showDialog) {
            AddTaskDialog(
                onDismiss = { showDialog = false },
                onSave = { title, subtasks, subtaskStates, date ->
                    viewModel.addTask(
                        Task(
                            content = title,
                            subtasks = subtasks,
                            subtaskStates = subtaskStates,
                            reminderDate = date
                        )
                    )
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (String, List<String>, List<Boolean>, String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var subtaskInput by remember { mutableStateOf("") }
    var subtasks by remember { mutableStateOf(listOf<String>()) }
    var subtaskStates by remember { mutableStateOf(listOf<Boolean>()) }
    var reminderDate by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, subtasks, subtaskStates, reminderDate)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("儲存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = {
            Text("📝 新增任務", fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任務標題") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        label = { Text("子任務") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (subtaskInput.isNotBlank()) {
                                subtasks = subtasks + subtaskInput
                                subtaskStates = subtaskStates + false
                                subtaskInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF9C6EF5), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "加入子任務",
                            tint = Color.White
                        )
                    }
                }

                if (subtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        subtasks.forEachIndexed { index, subtask ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFF4F4F4), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Checkbox(
                                    checked = subtaskStates.getOrNull(index) == true,
                                    onCheckedChange = {
                                        subtaskStates = subtaskStates.toMutableList().also { it[index] = !it[index] }
                                    }
                                )
                                Text(
                                    text = subtask,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    subtasks = subtasks.filterIndexed { i, _ -> i != index }
                                    subtaskStates = subtaskStates.filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "刪除子任務",
                                        tint = Color(0xFFF34F41)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        DatePickerDialog(context, { _, y, m, d ->
                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                            val minute = calendar.get(Calendar.MINUTE)

                            TimePickerDialog(context, { _, h, min ->
                                val selectedCalendar = Calendar.getInstance().apply {
                                    set(y, m, d, h, min)
                                }
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                reminderDate = sdf.format(selectedCalendar.time)
                            }, hour, minute, true).show()
                        }, year, month, day).show()
                    },

                            modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Event, contentDescription = "選擇日期")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = reminderDate ?: "設定提醒日期")
                }

                if (reminderDate != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistChip(
                        onClick = {},
                        label = {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val targetTime = sdf.parse(reminderDate!!)
                            val now = System.currentTimeMillis()
                            val diffDays = targetTime?.let {
                                ((it.time - now) / (1000 * 60 * 60 * 24)).toInt()
                            } ?: 0
                            Text("⏳ 倒數 $diffDays 天")
                        }
                    )
                }
            }
        }
    )
}
