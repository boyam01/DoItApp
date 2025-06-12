package com.yourname.doitapp.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.doitapp.data.Task

// --- 從 TaskListScreen 引入的風格化配色方案 ---
private val PrimaryPurple = Color(0xFF6C5CE7)
private val SoftWhite = Color(0xFFFDFCFF)
private val WarmGray = Color(0xFFF8F9FA)
private val DarkText = Color(0xFF2D3436)
private val LightGray = Color(0xFFDDD6FE)
private val DangerRed = Color(0xFFE17055)

// 成就等級資料類別
private data class AchievementLevel(val name: String, val minTasks: Int, val maxTasks: Int)

// 根據完成數量獲取對應的成就等級
private fun getAchievementLevel(count: Int): AchievementLevel {
    return when (count) {
        in 0..4 -> AchievementLevel("🌱 初學者", 0, 5)
        in 5..9 -> AchievementLevel("💪 累積達人", 5, 10)
        in 10..19 -> AchievementLevel("🔥 效率強者", 10, 20)
        else -> AchievementLevel("🏆 任務王者", 20, 30) // 假設王者等級的下一個目標是30
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoneScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val doneTasks = tasks.filter { it.isDone }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SoftWhite, WarmGray)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "成就殿堂",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "成就殿堂",
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                fontSize = 20.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // --- 成就總結卡片 ---
                item {
                    val achievement = getAchievementLevel(count)
                    val progress = animateFloatAsState(
                        targetValue = if (achievement.maxTasks > achievement.minTasks) {
                            (count - achievement.minTasks).toFloat() / (achievement.maxTasks - achievement.minTasks)
                        } else 0f,
                        animationSpec = tween(1000), label = ""
                    ).value

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "已完成 $count 項任務",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "目前等級：${achievement.name}",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // 進度條
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "距離下一等級",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        "${(progress * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        color = PrimaryPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = PrimaryPurple,
                                    trackColor = LightGray,
                                )
                            }
                        }
                    }
                }

                // --- 已完成任務列表 ---
                if (doneTasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "尚未有已完成的任務",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "繼續努力，在這裡累積你的成就！",
                                    color = Color.Gray.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    items(doneTasks, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        task.content,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = DarkText
                                        )
                                    )
                                    if (task.subtasks.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        task.subtasks.forEach { subtask ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.SubdirectoryArrowRight,
                                                    contentDescription = "子任務",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = subtask,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                                )
                                            }
                                        }
                                    }
                                    if (!task.reminderDate.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = "提醒日期",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "提醒於: ${task.reminderDate}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(
                                    onClick = { viewModel.deleteTask(task) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(DangerRed.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "刪除紀錄",
                                        tint = DangerRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}