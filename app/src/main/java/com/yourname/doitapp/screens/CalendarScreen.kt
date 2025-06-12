package com.yourname.doitapp.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.doitapp.data.Task // 確保 Task 類別的導入路徑正確
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

// --- 從 TaskListScreen 引入的風格化配色方案 ---
private val PrimaryPurple = Color(0xFF6C5CE7)
private val LightPurple = Color(0xFFA29BFE)
private val SoftWhite = Color(0xFFFDFCFF)
private val WarmGray = Color(0xFFF8F9FA)
private val DarkText = Color(0xFF2D3436)
private val LightGray = Color(0xFFDDD6FE)
private val AccentOrange = Color(0xFFFD79A8) // 用於提醒事項的點
private val SuccessGreen = Color(0xFF00B894)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: TaskViewModel = viewModel()) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    val dateOnlyFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    val dateTimeFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()) }

    val taskDates = remember(tasks) {
        tasks.mapNotNull { task ->
            try {
                task.reminderDate?.let {
                    val parsed = dateTimeFormatter.parse(it)
                    parsed?.let { dateOnlyFormatter.format(it) }
                }
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SoftWhite, WarmGray)
                )
            )
    ) {
        // 使用 LazyColumn 替代 Column 以便未來擴展和性能
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- Header Section: Month Navigation ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "上個月",
                                tint = PrimaryPurple
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = "${currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.TAIWAN)} ${currentMonth.year}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                            )
                        }
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "下個月",
                                tint = PrimaryPurple
                            )
                        }
                    }
                }
            }


            // --- Weekday Header ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
                    weekDays.forEach { day ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = day,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }


            // --- Calendar Grid (Animated Content) ---
            item {
                AnimatedContent(
                    targetState = currentMonth,
                    transitionSpec = {
                        fadeIn(tween(300, delayMillis = 50)) togetherWith fadeOut(tween(300))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { month ->
                    val firstDayOfMonth = month.atDay(1)
                    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1 + 7) % 7
                    val daysInMonth = month.lengthOfMonth()
                    val today = LocalDate.now()
                    val totalCells = (dayOfWeekOffset + daysInMonth + 6) / 7 * 7

                    Column {
                        for (i in 0 until totalCells step 7) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (j in 0..6) {
                                    val cellIndex = i + j
                                    val day = cellIndex - dayOfWeekOffset + 1
                                    val valid = day in 1..daysInMonth
                                    val date = if (valid) month.atDay(day) else null
                                    val isSelected = date == selectedDate
                                    val isToday = date == today
                                    val isReminderDay = date?.format(formatter) in taskDates

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp)
                                            .aspectRatio(1f)
                                            .clickable(enabled = valid) {
                                                date?.let { selectedDate = it }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (valid) {
                                            val cellBackgroundColor = when {
                                                isSelected -> PrimaryPurple
                                                isToday -> LightGray
                                                else -> Color.White
                                            }
                                            val textColor = when {
                                                isSelected -> Color.White
                                                isToday -> PrimaryPurple
                                                else -> DarkText
                                            }
                                            val borderModifier = if (isToday && !isSelected) {
                                                Modifier.border(
                                                    width = 1.5.dp,
                                                    color = PrimaryPurple,
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                            } else Modifier

                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .then(borderModifier),
                                                shape = RoundedCornerShape(16.dp),
                                                color = cellBackgroundColor,
                                                shadowElevation = if (isSelected) 6.dp else if (isToday) 2.dp else 1.dp
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text(
                                                        text = day.toString(),
                                                        color = textColor,
                                                        fontWeight = if (isToday || isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                    if (isReminderDay) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .background(
                                                                    AccentOrange,
                                                                    shape = CircleShape
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            item { Spacer(modifier = Modifier.height(20.dp)) }

            // --- Selected Date & Tasks Header ---
            item {
                Text(
                    text = "任務清單 - ${selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 E", Locale.TAIWAN))}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = DarkText,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                )
            }


            // --- Task List for Selected Date ---
            val taskList = viewModel.getTasksByDate(selectedDate.format(formatter))
            if (taskList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "當天沒有任務",
                                color = Color.Gray,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "享受悠閒時光吧！",
                                color = Color.Gray.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(count = taskList.size) { index ->
                    val task = taskList[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
                        ),
                        onClick = { viewModel.toggleTaskDone(task) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.content,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Medium,
                                        color = if (task.isDone) Color.Gray else DarkText
                                    ),
                                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                                )
                                task.reminderDate?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "提醒時間：${it.substring(it.indexOf(" ") + 1)}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (task.isDone) Color.Gray else DarkText.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}