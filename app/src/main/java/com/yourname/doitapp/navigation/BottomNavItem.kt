package com.yourname.doitapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Tasks : BottomNavItem("tasks", Icons.Filled.List, "任務")
    object Calendar : BottomNavItem("calendar", Icons.Filled.DateRange, "日曆")
    object Done : BottomNavItem("done", Icons.Filled.Done, "完成")
    object Pomodoro : BottomNavItem("pomodoro", Icons.Filled.Timer, "番茄鐘")
    object Profile : BottomNavItem("profile", Icons.Filled.Person, "我")
}