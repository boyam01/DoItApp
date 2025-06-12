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
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.doitapp.data.Task
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.app.TimePickerDialog

// 精心設計的配色方案
private val PrimaryPurple = Color(0xFF6C5CE7)
private val LightPurple = Color(0xFFA29BFE)
private val SoftWhite = Color(0xFFFDFCFF)
private val WarmGray = Color(0xFFF8F9FA)
private val DarkText = Color(0xFF2D3436)
private val LightGray = Color(0xFFDDD6FE)
private val AccentOrange = Color(0xFFFD79A8)
private val SuccessGreen = Color(0xFF00B894)
private val WarningYellow = Color(0xFFFDCB6E)
private val DangerRed = Color(0xFFE17055)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    // 計算完成度統計
    val completedTasks = tasks.count { it.isDone }
    val totalTasks = tasks.size
    val progressPercentage = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SoftWhite,
                        WarmGray
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "我的任務清單",
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
                                    fontSize = 20.sp
                                )
                                if (totalTasks > 0) {
                                    Text(
                                        "已完成 $completedTasks / $totalTasks 項任務",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.shadow(0.dp)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = PrimaryPurple,
                    contentColor = Color.White,
                    modifier = Modifier.shadow(8.dp, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "新增任務",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            content = { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        // 歡迎卡片
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TaskAlt,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "今天要做什麼？",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DarkText
                                            )
                                        )
                                        Text(
                                            text = "讓我們一起完成今天的目標吧！",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                if (totalTasks > 0) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 進度條
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "完成進度",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                "${(progressPercentage * 100).toInt()}%",
                                                fontSize = 12.sp,
                                                color = PrimaryPurple,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { progressPercentage },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp),
                                            color = PrimaryPurple,
                                            trackColor = LightGray,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (tasks.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = null,
                                        tint = Color.Gray.copy(alpha = 0.5f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "還沒有任務",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "點擊右下角的 + 按鈕來新增第一個任務",
                                        color = Color.Gray.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    } else {
                        items(tasks) { task ->
                            var expanded by remember { mutableStateOf(false) }
                            val taskId = task.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = if (task.isDone) 2.dp else 4.dp,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (task.isDone)
                                        Color.White.copy(alpha = 0.7f)
                                    else
                                        Color.White
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = task.isDone,
                                            onCheckedChange = { viewModel.toggleTaskDone(task) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = PrimaryPurple,
                                                uncheckedColor = Color.Gray,
                                                checkmarkColor = Color.White
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = task.content,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = if (task.isDone) Color.Gray else DarkText,
                                                fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Medium,
                                                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteTask(task) },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    DangerRed.copy(alpha = 0.1f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "刪除任務",
                                                tint = DangerRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    if (task.subtasks.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedButton(
                                            onClick = { expanded = !expanded },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = PrimaryPurple
                                            ),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                                brush = Brush.horizontalGradient(
                                                    listOf(PrimaryPurple, LightPurple)
                                                )
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SubdirectoryArrowRight,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (expanded) "隱藏子任務 (${task.subtasks.size})" else "查看子任務 (${task.subtasks.size})",
                                                fontSize = 12.sp
                                            )
                                        }

                                        if (expanded) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = WarmGray
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    task.subtasks.forEachIndexed { index, subtask ->
                                                        val checked = task.subtaskStates.getOrNull(index) == true
                                                        var editedText by remember { mutableStateOf(subtask) }

                                                        if (index > 0) {
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                        }

                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Checkbox(
                                                                checked = checked,
                                                                onCheckedChange = {
                                                                    viewModel.updateSubtaskState(taskId, index, !checked)
                                                                },
                                                                colors = CheckboxDefaults.colors(
                                                                    checkedColor = SuccessGreen,
                                                                    uncheckedColor = Color.Gray
                                                                )
                                                            )
                                                            OutlinedTextField(
                                                                value = editedText,
                                                                onValueChange = {
                                                                    editedText = it
                                                                    viewModel.updateSubtaskContent(taskId, index, it)
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                                                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                                                                ),
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    focusedBorderColor = PrimaryPurple,
                                                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                                                                ),
                                                                shape = RoundedCornerShape(8.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            IconButton(
                                                                onClick = {
                                                                    viewModel.removeSubtask(taskId, index)
                                                                },
                                                                modifier = Modifier.size(32.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = "刪除子任務",
                                                                    tint = DangerRed,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    task.reminderDate?.let { dateStr ->
                                        Spacer(modifier = Modifier.height(16.dp))
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        val targetDate = try { sdf.parse(dateStr) } catch (e: Exception) { null }
                                        val daysLeft = targetDate?.let {
                                            val diff = it.time - System.currentTimeMillis()
                                            TimeUnit.MILLISECONDS.toDays(diff).toInt()
                                        }
                                        val (chipText, chipColor) = when {
                                            daysLeft == null -> "❓ 未設定" to Color.LightGray
                                            daysLeft > 0 -> "⏳ 還有 $daysLeft 天" to SuccessGreen
                                            daysLeft == 0 -> "⚠️ 今天到期" to WarningYellow
                                            else -> "❌ 已過期" to DangerRed
                                        }

                                        AssistChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    chipText,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = chipColor
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp)) // 為 FAB 留出空間
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
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "儲存",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Gray
                )
            ) {
                Text("取消")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "新增任務",
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任務標題") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        label = { Text("子任務") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (subtaskInput.isNotBlank()) {
                                subtasks = subtasks + subtaskInput
                                subtaskStates = subtaskStates + false
                                subtaskInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "加入子任務",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (subtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = WarmGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            subtasks.forEachIndexed { index, subtask ->
                                if (index > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = subtaskStates.getOrNull(index) == true,
                                        onCheckedChange = {
                                            subtaskStates = subtaskStates.toMutableList().also { it[index] = !it[index] }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = PrimaryPurple
                                        )
                                    )
                                    Text(
                                        text = subtask,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(
                                        onClick = {
                                            subtasks = subtasks.filterIndexed { i, _ -> i != index }
                                            subtaskStates = subtaskStates.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "刪除子任務",
                                            tint = DangerRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = "選擇日期",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reminderDate ?: "設定提醒日期",
                        fontWeight = FontWeight.Medium
                    )
                }

                if (reminderDate != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AssistChip(
                        onClick = {},
                        label = {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val targetTime = sdf.parse(reminderDate!!)
                            val now = System.currentTimeMillis()
                            val diffDays = targetTime?.let {
                                ((it.time - now) / (1000 * 60 * 60 * 24)).toInt()
                            } ?: 0
                            Text(
                                "⏳ 倒數 $diffDays 天",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SuccessGreen
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
