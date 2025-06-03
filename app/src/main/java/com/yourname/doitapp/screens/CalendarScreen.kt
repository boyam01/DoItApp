package com.yourname.doitapp.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(viewModel: TaskViewModel = viewModel()) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val taskDates = tasks.mapNotNull { it.reminderDate }.toSet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header: 上一個月 / 月份顯示 / 下一個月
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "上一個月"
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 6.dp)
                )
                Text(
                    text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "下一個月"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 星期標題
        val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = day, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 用 AnimatedContent 讓月曆轉場順暢
        AnimatedContent(
            targetState = currentMonth,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            }
        ) { month ->
            val firstDayOfMonth = month.atDay(1)
            val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value % 7)
            val daysInMonth = month.lengthOfMonth()
            val today = LocalDate.now()
            val totalCells = ((dayOfWeekOffset + daysInMonth + 6) / 7) * 7

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
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF5F5F5),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = valid) {
                                        date?.let { selectedDate = it }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (valid) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day.toString(),
                                            color = if (isSelected) Color.White else Color.Black,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isReminderDay) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFF4CAF50), shape = CircleShape)
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

        Spacer(modifier = Modifier.height(16.dp))

        // 顯示選定日期
        Text(
            text = "選擇日期：${selectedDate.format(formatter)}",
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 取得並顯示該日期的任務
        val taskList = viewModel.getTasksByDate(selectedDate.format(formatter))
        if (taskList.isEmpty()) {
            Text(text = "當天沒有任務", color = Color.Gray, modifier = Modifier.padding(8.dp))
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                taskList.forEach { task ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.elevatedCardElevation(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { viewModel.toggleTaskDone(task) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = task.content, style = MaterialTheme.typography.bodyLarge)
                                task.reminderDate?.let {
                                    Text(
                                        text = "提醒日：$it",
                                        style = MaterialTheme.typography.labelSmall
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
