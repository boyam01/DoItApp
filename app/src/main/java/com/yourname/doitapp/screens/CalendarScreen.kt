package com.yourname.doitapp.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

// Dummy data classes and ViewModel for demonstration.
// In a real app, these would be in their respective packages.



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(viewModel: TaskViewModel = viewModel()) {
    // State to hold the currently selected date, initialized to today.
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    // State to hold the month currently displayed in the calendar.
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Formatter for "yyyy-MM-dd" date strings.
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    // Collect tasks from the ViewModel's StateFlow.
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    // SimpleDateFormat instances for parsing and formatting date strings from reminders.
    // Use remember to avoid recreating them on every recomposition.
    val dateOnlyFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    val dateTimeFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()) }

    // Set of dates that have associated tasks, used to display reminder dots on the calendar.
    val taskDates = remember(tasks) { // Re-calculate this set whenever the 'tasks' list changes.
        tasks.mapNotNull { reminder ->
            try {
                reminder.reminderDate?.let {
                    // Parse the full date-time string and extract only the date part.
                    val parsed = dateTimeFormatter.parse(it)
                    parsed?.let { dateOnlyFormatter.format(it) }
                }
            } catch (e: Exception) {
                // Handle parsing errors, e.g., log them or show a message.
                null
            }
        }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use the theme's background color for the screen.
            .padding(horizontal = 16.dp, vertical = 24.dp) // Generous padding for a clean look.
    ) {
        // --- Header Section: Month Navigation ---
        // A visually distinct surface for the month navigation.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp), // Rounded corners for a modern card-like appearance.
            shadowElevation = 8.dp, // Adds a subtle shadow for depth.
            color = MaterialTheme.colorScheme.surfaceVariant // A slightly different background for the header.
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween, // Distributes elements evenly.
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Previous Month Button
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant // Icon color from theme.
                    )
                }
                // Month and Year Display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null, // Content description can be null for decorative icons.
                        tint = MaterialTheme.colorScheme.primary, // Calendar icon uses primary color.
                        modifier = Modifier
                            .size(24.dp) // Larger icon size.
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "${currentMonth.month.name.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase(Locale.getDefault()) }} ${currentMonth.year}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), // Prominent font for month/year.
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Next Month Button
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- Weekday Header ---
        // Displays "Mon, Tue, Wed..." at the top of the calendar grid.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)) // Background with rounded corners.
                .padding(vertical = 8.dp)
        ) {
            val weekDays = listOf("一", "二", "三", "四", "五", "六", "日") // Weekday labels.
            weekDays.forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium // Consistent typography.
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp)) // Spacing between weekday header and calendar grid.

        // --- Calendar Grid (Animated Content) ---
        // Uses AnimatedContent for smooth transitions when switching months.
        AnimatedContent(
            targetState = currentMonth,
            transitionSpec = {
                // Fade in and out animation for month changes.
                fadeIn(tween(300, delayMillis = 50)) togetherWith fadeOut(tween(300))
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Allows the calendar grid to expand and fill available space.
        ) { month ->
            val firstDayOfMonth = month.atDay(1)
            // Calculate the offset for the first day of the month (0 for Monday, 6 for Sunday).
            // Adjusting for Monday as the first day of the week (ISO 8601 standard).
            val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1 + 7) % 7
            val daysInMonth = month.lengthOfMonth()
            val today = LocalDate.now()
            // Calculate total cells needed to display full weeks, including leading/trailing empty cells.
            val totalCells = (dayOfWeekOffset + daysInMonth + 6) / 7 * 7

            Column {
                // Iterate through rows of the calendar grid.
                for (i in 0 until totalCells step 7) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Iterate through days within each week.
                        for (j in 0..6) {
                            val cellIndex = i + j
                            // Calculate the day number for the current cell.
                            val day = cellIndex - dayOfWeekOffset + 1
                            // Check if the day is valid for the current month.
                            val valid = day in 1..daysInMonth
                            // Get the LocalDate for the current cell if valid, otherwise null.
                            val date = if (valid) month.atDay(day) else null
                            // Determine various states for styling.
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            val isReminderDay = date?.format(formatter) in taskDates

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .aspectRatio(1f) // Ensures cells are square, maintaining a clean grid.
                                    .clickable(enabled = valid) { // Only clickable if it's a valid day in the month.
                                        date?.let { selectedDate = it } // Update selected date on click.
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (valid) { // Only draw content for valid days.
                                    val cellBackgroundColor = when {
                                        isSelected -> MaterialTheme.colorScheme.primary // Highlight selected date.
                                        isToday -> MaterialTheme.colorScheme.secondaryContainer // Highlight today's date.
                                        else -> MaterialTheme.colorScheme.surface // Default background for other days.
                                    }
                                    val textColor = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary // Text color for selected date.
                                        isToday -> MaterialTheme.colorScheme.onSecondaryContainer // Text color for today's date.
                                        else -> MaterialTheme.colorScheme.onSurface // Default text color.
                                    }
                                    // Add a subtle border to today's date if it's not selected.
                                    val borderModifier = if (isToday && !isSelected) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    } else Modifier

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize() // Fills the available box space.
                                            .then(borderModifier), // Apply border if it's today and not selected.
                                        shape = RoundedCornerShape(16.dp), // Rounded corners for date cells.
                                        color = cellBackgroundColor,
                                        shadowElevation = if (isSelected) 6.dp else 0.dp // Add elevation to selected date.
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                color = textColor,
                                                // Make text bold for today or selected date.
                                                fontWeight = if (isToday || isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (isReminderDay) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // Small dot to indicate a reminder on this day.
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp) // Larger dot for visibility.
                                                        .background(
                                                            MaterialTheme.colorScheme.tertiary, // A distinct color for reminder dots.
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

        Spacer(modifier = Modifier.height(20.dp)) // Spacing before the task list header.

        // --- Selected Date & Tasks Header ---
        Text(
            text = "任務清單 - ${selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 E"))}", // Full date with day of week.
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- Task List for Selected Date ---
        val taskList = viewModel.getTasksByDate(selectedDate.format(formatter))
        if (taskList.isEmpty()) {
            Text(
                text = "當天沒有任務，享受悠閒時光吧！", // Friendly message for no tasks.
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                taskList.forEach { task ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp), // Vertical spacing between task cards.
                        elevation = CardDefaults.elevatedCardElevation(4.dp), // More pronounced shadow.
                        shape = RoundedCornerShape(16.dp), // Rounded corners for task cards.
                        colors = CardDefaults.elevatedCardColors(
                            // Background color changes based on task completion status.
                            containerColor = if (task.isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable { viewModel.toggleTaskDone(task) } // Toggle task status by clicking the card.
                        ) {
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { viewModel.toggleTaskDone(task) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary, // Primary color for checked state.
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant // Theme color for unchecked state.
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) { // Takes remaining space.
                                Text(
                                    text = task.content,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        // Text color and strikethrough based on completion.
                                        color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                    ),
                                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                                )
                                task.reminderDate?.let {
                                    Text(
                                        text = "時間：${it.substring(it.indexOf(" ") + 1)}", // Only display the time part.
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
