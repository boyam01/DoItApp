package com.yourname.doitapp.screens

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.yourname.doitapp.R
import androidx.compose.foundation.background

@Composable
fun PomodoroScreen() {
    val context = LocalContext.current

    var isRunning by remember { mutableStateOf(false) }
    var isWorkTime by remember { mutableStateOf(true) }
    var remainingTime by remember { mutableStateOf(25 * 60) }
    var focusCount by remember { mutableIntStateOf(0) }

    var selectedSound by remember { mutableStateOf("None") }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(selectedSound) {
        mediaPlayer.value?.stop()
        mediaPlayer.value?.release()

        mediaPlayer.value = when (selectedSound) {
            "Rain" -> MediaPlayer.create(context, R.raw.rain)
            "Fire" -> MediaPlayer.create(context, R.raw.fire)
            "Wind" -> MediaPlayer.create(context, R.raw.wind)
            "Waterfall" -> MediaPlayer.create(context, R.raw.waterfall)
            else -> null
        }
        mediaPlayer.value?.isLooping = true
        mediaPlayer.value?.start()
    }

    LaunchedEffect(isRunning) {
        while (isRunning && remainingTime > 0) {
            delay(1000)
            remainingTime--
            if (remainingTime == 0) {
                isRunning = false
                if (isWorkTime) focusCount++
            }
        }
    }

    val minutes = remainingTime / 60
    val seconds = remainingTime % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("番茄鐘", fontSize = 28.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Image(painter = painterResource(id = R.drawable.tomato), contentDescription = null, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(16.dp))

        Text("%02d:%02d".format(minutes, seconds), fontSize = 48.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Text("專注次數：$focusCount")
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { isRunning = !isRunning }) {
            Text(if (isRunning) "暫停" else "開始")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showTimeDialog = true }) {
                Text("設定時間")
            }
            Button(onClick = { showSoundDialog = true }) {
                Text("白噪音")
            }
        }
    }

    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("選擇時間") },
            confirmButton = {},
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Pair(25, 5), Pair(50, 10), Pair(90, 20)).forEach { (work, rest) ->
                        Button(onClick = {
                            isWorkTime = true
                            remainingTime = work * 60
                            showTimeDialog = false
                        }) {
                            Text("專注 $work 分鐘 / 休息 $rest 分鐘")
                        }
                    }
                }
            }
        )
    }

    if (showSoundDialog) {
        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            title = { Text("選擇白噪音") },
            confirmButton = {},
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("None", "Rain", "Fire", "Wind", "Waterfall").forEach { label ->
                        Button(onClick = {
                            selectedSound = label
                            showSoundDialog = false
                        }) {
                            Text(label)
                        }
                    }
                }
            }
        )
    }
}
