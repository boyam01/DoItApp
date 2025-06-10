package com.yourname.doitapp.screens

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Stop // 新增停止圖標
import androidx.compose.material.icons.filled.VolumeUp // 音量圖標
import androidx.compose.material.icons.outlined.CheckCircleOutline // 對話框勾選
import androidx.compose.material.icons.outlined.Cancel // 對話框取消
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily // 新增字體
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.yourname.doitapp.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview

// 假設你新增了新的字體檔案，例如：
// 在 res/font/ 資料夾下放置 'Montserrat_Regular.ttf' 和 'Montserrat_SemiBold.ttf' 等
// val Montserrat = FontFamily(
//     Font(R.font.montserrat_regular),
//     Font(R.font.montserrat_semibold, FontWeight.SemiBold),
//     Font(R.font.montserrat_bold, FontWeight.Bold)
// )
// 如果沒有，請註釋掉 FontFamily 相關代碼，並使用預設字體

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PomodoroScreen() {
    val context = LocalContext.current

    var isRunning by remember { mutableStateOf(false) }
    var isWorkTime by remember { mutableStateOf(true) } // 決定目前是工作時間還是休息時間
    var workDuration by remember { mutableIntStateOf(25 * 60) } // 預設工作時間25分鐘
    var breakDuration by remember { mutableIntStateOf(5 * 60) } // 預設休息時間5分鐘
    var remainingTime by remember { mutableIntStateOf(workDuration) } // 剩餘時間
    var focusCount by remember { mutableIntStateOf(0) } // 專注次數

    var selectedSound by remember { mutableStateOf("無") } // 選擇的白噪音
    var showTimeDialog by remember { mutableStateOf(false) } // 是否顯示時間設定對話框
    var showSoundDialog by remember { mutableStateOf(false) } // 是否顯示白噪音設定對話框
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) } // 媒體播放器

    // 定義顏色主題，更柔和、現代的配色，注重細節與層次感
    val primaryLight = Color(0xFFFFA726) // 淺橙色，活力但不刺眼
    val primaryDark = Color(0xFFFB8C00)  // 深橙色，強調色
    val secondaryLight = Color(0xFF66BB6A) // 淺綠色，休息時用
    val secondaryDark = Color(0xFF43A047)  // 深綠色，休息強調色

    val backgroundGradientStart = Color(0xFFFFF3E0) // 背景漸層開始
    val backgroundGradientEnd = Color(0xFFFFCC80) // 背景漸層結束

    val textColorPrimary = Color(0xFF424242) // 深灰色文字
    val textColorSecondary = Color(0xFF757575) // 淺灰色提示文字

    // 處理白噪音播放邏輯
    LaunchedEffect(selectedSound) {
        mediaPlayer.value?.stop()
        mediaPlayer.value?.release() // 釋放舊的 MediaPlayer 實例

        mediaPlayer.value = when (selectedSound) {
            "雨聲" -> MediaPlayer.create(context, R.raw.rain)
            "火焰" -> MediaPlayer.create(context, R.raw.fire)
            "風聲" -> MediaPlayer.create(context, R.raw.wind)
            "瀑布" -> MediaPlayer.create(context, R.raw.waterfall)
            else -> null
        }
        mediaPlayer.value?.isLooping = true // 循環播放背景音
        mediaPlayer.value?.setVolume(0.2f, 0.2f) // 調低背景音量，更像環境音
        mediaPlayer.value?.start()
    }

    // 番茄鐘計時邏輯
    LaunchedEffect(isRunning, remainingTime, isWorkTime) {
        while (isRunning && remainingTime > 0) {
            delay(1000)
            remainingTime--
            if (remainingTime == 0) {
                isRunning = false // 時間到，停止計時
                if (isWorkTime) {
                    focusCount++ // 工作時間結束，專注次數加一
                    isWorkTime = false
                    remainingTime = breakDuration
                    // 播放提示音，提醒使用者切換階段
                    // 確保 R.raw.chime_sound 存在
                    MediaPlayer.create(context, R.raw.chime_sound)?.start()
                } else {
                    isWorkTime = true
                    remainingTime = workDuration
                    // 播放提示音
                    MediaPlayer.create(context, R.raw.chime_sound)?.start()
                }
            }
        }
    }

    // 番茄圖標的縮放動畫，在計時時增加動態感，更平滑的動畫曲線
    val scale = remember { Animatable(1f) }
    LaunchedEffect(isRunning, isWorkTime) {
        if (isRunning) {
            scale.animateTo(1.05f, animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1500 // 稍微慢一點，更平穩
                    1.0f at 0 with LinearOutSlowInEasing
                    1.05f at 750 with LinearOutSlowInEasing
                    1.0f at 1500 with LinearOutSlowInEasing
                },
                repeatMode = RepeatMode.Reverse
            ))
        } else {
            scale.stop()
            scale.animateTo(1f, animationSpec = tween(durationMillis = 300)) // 停止後平滑回到原始大小
        }
    }

    val minutes = remainingTime / 60
    val seconds = remainingTime % 60

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(listOf(backgroundGradientStart, backgroundGradientEnd)))
                .padding(paddingValues)
                .padding(horizontal = 28.dp, vertical = 36.dp), // 增加整體內邊距，提供更多呼吸空間
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 頂部區塊：標題和狀態提示
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🍅 番茄工作法",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary,
                    // fontFamily = Montserrat // 如果有自定義字體
                )
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedContent(
                    targetState = isWorkTime,
                    transitionSpec = {
                        slideInVertically(animationSpec = tween(300, easing = EaseOutCubic)) + fadeIn() with
                                slideOutVertically(targetOffsetY = { fullHeight -> fullHeight / 2 }, animationSpec = tween(300, easing = EaseInCubic)) + fadeOut()
                    }, label = "WorkBreakStatus"
                ) { targetIsWorkTime ->
                    Text(
                        text = if (targetIsWorkTime) "專注時間" else "休息時間",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (targetIsWorkTime) primaryDark else secondaryDark,
                        // fontFamily = Montserrat // 如果有自定義字體
                    )
                }
            }

            // 中間區塊：番茄圖標和時間顯示
            Box(
                modifier = Modifier
                    .size(220.dp) // 更大的尺寸，更有視覺衝擊力
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)) // 更高的透明度，更柔和
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape) // 添加柔和的邊框，增加層次感
                    .wrapContentSize(Alignment.Center)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tomato),
                    contentDescription = "番茄圖標",
                    modifier = Modifier
                        .size(160.dp) // 調整番茄圖標大小
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                )
            }
            Spacer(modifier = Modifier.height(28.dp))

            // 時間顯示
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                fontSize = 88.sp, // 超大字體，視覺焦點
                fontWeight = FontWeight.ExtraBold,
                color = textColorPrimary,
                // fontFamily = Montserrat // 如果有自定義字體
            )
            Spacer(modifier = Modifier.height(18.dp))

            // 專注次數
            Text(
                text = "已完成專注：$focusCount 次",
                fontSize = 18.sp,
                color = textColorSecondary, // 較淺的顏色，作為輔助信息
                // fontFamily = Montserrat // 如果有自定義字體
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 控制按鈕區塊
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // 讓按鈕整體居中
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 重置按鈕 (新增，提供更多控制)
                IconButton(
                    onClick = {
                        isRunning = false
                        isWorkTime = true
                        remainingTime = workDuration
                        mediaPlayer.value?.stop() // 停止白噪音
                        mediaPlayer.value?.seekTo(0) // 重置白噪音播放位置
                    },
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape) // 增加邊框細節
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "重置",
                        tint = primaryLight,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp)) // 間距

                // 開始/暫停按鈕
                FloatingActionButton(
                    onClick = { isRunning = !isRunning },
                    containerColor = if (isRunning) primaryDark else secondaryDark, // 根據狀態變色
                    contentColor = Color.White,
                    modifier = Modifier.size(76.dp), // 更大的按鈕
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp) // 增加陰影，更立體
                ) {
                    AnimatedContent(
                        targetState = isRunning,
                        transitionSpec = {
                            scaleIn(animationSpec = tween(300, easing = EaseOutBack)) + fadeIn() with
                                    scaleOut(animationSpec = tween(300, easing = EaseInBack)) + fadeOut()
                        }, label = "PlayPauseIcon"
                    ) { targetIsRunning ->
                        Icon(
                            imageVector = if (targetIsRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (targetIsRunning) "暫停" else "開始",
                            modifier = Modifier.size(52.dp) // 圖標大小
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp)) // 間距

                // 設定按鈕區塊
                IconButton(
                    onClick = { showTimeDialog = true },
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape) // 增加邊框細節
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "設定時間",
                        tint = primaryLight,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp)) // 與底部的間距

            // 白噪音按鈕獨立一行，強調其輔助功能
            Button(
                onClick = { showSoundDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = backgroundGradientStart.copy(alpha = 0.7f)), // 與背景色融合，更柔和
                modifier = Modifier.fillMaxWidth(0.6f), // 寬度稍窄，更精緻
                shape = RoundedCornerShape(24.dp), // 圓角按鈕
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp) // 輕微陰影
            ) {
                Icon(
                    imageVector = Icons.Filled.VolumeUp, // 音量圖標更貼切白噪音
                    contentDescription = "白噪音",
                    tint = textColorPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "白噪音: $selectedSound",
                    color = textColorPrimary.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    // fontFamily = Montserrat
                )
            }
        }
    }

    // 時間設定對話框 (子視窗)
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            containerColor = Color.White.copy(alpha = 0.95f), // 淺白背景，輕微透明
            shape = RoundedCornerShape(28.dp), // 大圓角
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = "時間設定",
                        tint = primaryDark,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "選擇專注與休息時間",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textColorPrimary,
                        // fontFamily = Montserrat
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { // 增加間距
                    listOf(
                        Triple(25, 5, "標準 (25/5 分鐘)"),
                        Triple(50, 10, "深度 (50/10 分鐘)"),
                        Triple(90, 20, "馬拉松 (90/20 分鐘)")
                    ).forEach { (work, rest, label) ->
                        Button(
                            onClick = {
                                workDuration = work * 60
                                breakDuration = rest * 60
                                remainingTime = workDuration
                                isWorkTime = true
                                isRunning = false
                                showTimeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (workDuration == work * 60) primaryLight else Color.LightGray.copy(alpha = 0.4f)
                            ), // 選中時亮色，未選中時柔和
                            shape = RoundedCornerShape(20.dp), // 圓角按鈕
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp) // 輕微陰影
                        ) {
                            Text(
                                label,
                                color = if (workDuration == work * 60) Color.White else textColorPrimary.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                // fontFamily = Montserrat
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Icon(Icons.Outlined.Cancel, contentDescription = "取消", tint = textColorSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("取消", color = textColorSecondary, fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    // 白噪音選擇對話框 (子視窗)
    if (showSoundDialog) {
        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            containerColor = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(28.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = "白噪音",
                        tint = secondaryDark,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "選擇白噪音",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textColorPrimary,
                        // fontFamily = Montserrat
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    listOf("無", "雨聲", "火焰", "風聲", "瀑布").forEach { label ->
                        Button(
                            onClick = {
                                selectedSound = label
                                showSoundDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSound == label) secondaryLight else Color.LightGray.copy(alpha = 0.4f)
                            ), // 選中時亮色，未選中時柔和
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                label,
                                color = if (selectedSound == label) Color.White else textColorPrimary.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                // fontFamily = Montserrat
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSoundDialog = false }) {
                    Icon(Icons.Outlined.Cancel, contentDescription = "取消", tint = textColorSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("取消", color = textColorSecondary, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPomodoroScreen() {
    PomodoroScreen()
}