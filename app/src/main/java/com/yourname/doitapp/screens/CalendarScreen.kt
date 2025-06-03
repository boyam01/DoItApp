package com.yourname.doitapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(viewModel: TaskViewModel = viewModel()) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = YearMonth.now()
    val daysInMonth = currentMonth.lengthOfMonth()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val tasks by viewModel.tasks.collectAsState()
    val taskDates = tasks.mapNotNull { it.reminderDate }.toSet()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("${currentMonth.month} ${currentMonth.year}", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        for (weekStart in 1..daysInMonth step 7) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (day in weekStart until (weekStart + 7).coerceAtMost(daysInMonth + 1)) {
                    val date = currentMonth.atDay(day)
                    val isSelected = date == selectedDate
                    val isReminderDay = taskDates.contains(date.format(formatter))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isReminderDay -> Color(0xFFBEE3DB)
                                    else -> Color.LightGray
                                },
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { selectedDate = date },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("選擇日期：$selectedDate")

        val taskList = viewModel.getTasksByDate(selectedDate.format(formatter))
        Column {
            taskList.forEach { task ->
                Text("• ${task.content}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
