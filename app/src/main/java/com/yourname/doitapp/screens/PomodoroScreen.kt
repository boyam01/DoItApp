package com.yourname.doitapp.screens

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.doitapp.R
import kotlinx.coroutines.delay

// --- 統一的應用程式配色方案 ---
private val PrimaryPurple = Color(0xFF6C5CE7)
private val LightPurple = Color(0xFFA29BFE)
private val SoftWhite = Color(0xFFFDFCFF)
private val WarmGray = Color(0xFFF8F9FA)
private val DarkText = Color(0xFF2D3436)
private val LightGray = Color(0xFFDDD6FE)
private val SuccessGreen = Color(0xFF00B894) // 用於休息時間狀態

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen() {
    val context = LocalContext.current

    var isRunning by remember { mutableStateOf(false) }
    var isWorkTime by remember { mutableStateOf(true) }
    var workDuration by remember { mutableIntStateOf(25 * 60) }
    var breakDuration by remember { mutableIntStateOf(5 * 60) }
    var remainingTime by remember { mutableIntStateOf(workDuration) }
    var focusCount by remember { mutableIntStateOf(0) }

    var selectedSound by remember { mutableStateOf("無") }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    // 處理白噪音播放邏輯
    LaunchedEffect(selectedSound, isRunning) {
        if (selectedSound == "無" || !isRunning) {
            mediaPlayer.value?.pause()
            return@LaunchedEffect
        }

        mediaPlayer.value?.stop()
        mediaPlayer.value?.release()

        mediaPlayer.value = when (selectedSound) {
            "雨聲" -> MediaPlayer.create(context, R.raw.rain)
            "火焰" -> MediaPlayer.create(context, R.raw.fire)
            "風聲" -> MediaPlayer.create(context, R.raw.wind)
            "瀑布" -> MediaPlayer.create(context, R.raw.waterfall)
            else -> null
        }?.apply {
            isLooping = true
            setVolume(0.2f, 0.2f)
            if (isRunning) start()
        }
    }

    // 番茄鐘計時邏輯
    LaunchedEffect(isRunning, remainingTime, isWorkTime) {
        while (isRunning && remainingTime > 0) {
            delay(1000)
            remainingTime--
            if (remainingTime == 0) {
                isRunning = false
                val sound = MediaPlayer.create(context, R.raw.chime_sound)
                sound?.start()
                sound?.setOnCompletionListener { it.release() }

                if (isWorkTime) {
                    focusCount++
                    isWorkTime = false
                    remainingTime = breakDuration
                } else {
                    isWorkTime = true
                    remainingTime = workDuration
                }
            }
        }
    }

    // 番茄圖標的縮放動畫
    val scale = remember { Animatable(1f) }
    LaunchedEffect(isRunning) {
        if (isRunning) {
            scale.animateTo(1.05f, infiniteRepeatable(
                animation = tween(1500, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ))
        } else {
            scale.stop()
            scale.animateTo(1f, tween(300))
        }
    }

    val minutes = remainingTime / 60
    val seconds = remainingTime % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(SoftWhite, WarmGray)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(id = R.drawable.tomato),
                                contentDescription = "番茄工作法",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("番茄工作法", fontWeight = FontWeight.Bold, color = DarkText, fontSize = 20.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 28.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround // 讓元件分佈更均勻
            ) {
                // 狀態提示 (美化後)
                AnimatedContent(
                    targetState = isWorkTime,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) + slideInVertically { it / 2 } with
                                fadeOut(animationSpec = tween(300)) + slideOutVertically { -it / 2 }
                    }, label = "WorkBreakStatus"
                ) { targetIsWorkTime ->
                    val color = if (targetIsWorkTime) PrimaryPurple else SuccessGreen
                    Surface(
                        shape = CircleShape,
                        color = color.copy(alpha = 0.1f), // 柔和的背景色
                        border = BorderStroke(1.dp, color) // 與文字同色的邊框
                    ) {
                        Text(
                            text = if (targetIsWorkTime) "保持專注" else "休息一下",
                            fontSize = 18.sp, // 稍微調整字體大小以適應標籤
                            fontWeight = FontWeight.SemiBold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                // 中間番茄圖標
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(250.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.7f),
                        shadowElevation = 8.dp
                    ) {}
                    Image(
                        painter = painterResource(id = R.drawable.tomato),
                        contentDescription = "番茄圖標",
                        modifier = Modifier
                            .size(160.dp)
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                    )
                }

                // 時間與專注次數
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        fontSize = 88.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkText,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // 專注次數 (美化後)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(LightGray.copy(alpha = 0.4f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star, // 使用星星圖示，更具成就感
                            contentDescription = "專注次數",
                            tint = PrimaryPurple.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "已完成專注：$focusCount 次",
                            fontSize = 14.sp,
                            color = DarkText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 控制按鈕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            isRunning = false
                            isWorkTime = true
                            remainingTime = workDuration
                            mediaPlayer.value?.seekTo(0)
                        },
                        modifier = Modifier
                            .size(60.dp)
                            .background(LightGray.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Stop, "重置", tint = PrimaryPurple, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    FloatingActionButton(
                        onClick = { isRunning = !isRunning },
                        containerColor = PrimaryPurple,
                        contentColor = Color.White,
                        modifier = Modifier.size(76.dp),
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        AnimatedContent(
                            targetState = isRunning, label = "PlayPauseIcon",
                            transitionSpec = { scaleIn() with scaleOut() }
                        ) { targetIsRunning ->
                            Icon(
                                if (targetIsRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                if (targetIsRunning) "暫停" else "開始",
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    IconButton(
                        onClick = { showTimeDialog = true },
                        modifier = Modifier
                            .size(60.dp)
                            .background(LightGray.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Settings, "設定時間", tint = PrimaryPurple, modifier = Modifier.size(32.dp))
                    }
                }

                // 白噪音按鈕
                OutlinedButton(
                    onClick = { showSoundDialog = true },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(PrimaryPurple, LightPurple))
                    )
                ) {
                    Icon(Icons.Filled.VolumeUp, "白噪音", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("白噪音: $selectedSound", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    val dialogShape = RoundedCornerShape(28.dp)

    // 時間設定對話框
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            containerColor = Color.White,
            shape = dialogShape,
            // 時間設定對話框標題 (美化後)
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = "時間設定",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("選擇專注與休息時間", fontWeight = FontWeight.Bold, color = DarkText)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        Triple(25, 5, "標準 (25/5 分鐘)"),
                        Triple(50, 10, "深度 (50/10 分鐘)"),
                        Triple(90, 20, "馬拉松 (90/20 分鐘)")
                    ).forEach { (work, rest, label) ->
                        val isSelected = workDuration == work * 60
                        val action = {
                            workDuration = work * 60
                            breakDuration = rest * 60
                            remainingTime = workDuration
                            isWorkTime = true
                            isRunning = false
                            showTimeDialog = false
                        }
                        if (isSelected) {
                            Button(onClick = action, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)) {
                                Text(label, color = Color.White)
                            }
                        } else {
                            OutlinedButton(onClick = action, modifier = Modifier.fillMaxWidth()) {
                                Text(label, color = PrimaryPurple)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTimeDialog = false }) { Text("取消", color = Color.Gray) } }
        )
    }

    // 白噪音選擇對話框
    if (showSoundDialog) {
        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            containerColor = Color.White,
            shape = dialogShape,
            // 白噪音選擇對話框標題 (美化後)
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = "白噪音設定",
                        tint = SuccessGreen, // 使用綠色來匹配「休息」的感覺
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("選擇白噪音", fontWeight = FontWeight.Bold, color = DarkText)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("無", "雨聲", "火焰", "風聲", "瀑布").forEach { label ->
                        val isSelected = selectedSound == label
                        val action = {
                            selectedSound = label
                            showSoundDialog = false
                        }

                        if (isSelected) {
                            Button(onClick = action, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                                Text(label, color = Color.White)
                            }
                        } else {
                            OutlinedButton(onClick = action, modifier = Modifier.fillMaxWidth()) {
                                Text(label, color = SuccessGreen)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSoundDialog = false }) { Text("取消", color = Color.Gray) } }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPomodoroScreen() {
    PomodoroScreen()
}