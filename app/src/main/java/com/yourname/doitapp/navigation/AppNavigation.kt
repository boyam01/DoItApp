package com.yourname.doitapp.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yourname.doitapp.screens.*
import androidx.compose.ui.draw.clip


@Composable
fun AppNavigation(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Tasks,
        BottomNavItem.Calendar,
        BottomNavItem.Done,
        BottomNavItem.Pomodoro,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            BottomAppBarContainer(navController, items)
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Tasks.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Tasks.route) { TaskListScreen() }
            composable(BottomNavItem.Calendar.route) { CalendarScreen() }
            composable(BottomNavItem.Done.route) { DoneScreen() }
            composable(BottomNavItem.Pomodoro.route) { PomodoroScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen() }
        }
    }
}

@Composable
fun BottomAppBarContainer(navController: NavHostController, items: List<BottomNavItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val backgroundColor = Color.White.copy(alpha = 0.9f)
    val unselectedColor = Color(0xFFB0BEC5)

    val itemEmojis = mapOf(
        BottomNavItem.Tasks.route to "📝",
        BottomNavItem.Calendar.route to "📅",
        BottomNavItem.Done.route to "✅",
        BottomNavItem.Pomodoro.route to "⏱️",
        BottomNavItem.Profile.route to "👤"
    )

    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(72.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp), clip = false) // ✅ 陰影
            .clip(RoundedCornerShape(24.dp)),
        color = backgroundColor,
        tonalElevation = 0.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val color = if (selected) Color.Black else unselectedColor
                val emoji = itemEmojis[item.route] ?: "❓"

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    if (selected) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    }
                }
            }
        }
    }
}
