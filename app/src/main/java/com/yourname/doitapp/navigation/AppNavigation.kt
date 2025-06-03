package com.yourname.doitapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    val backgroundColor = Color.White.copy(alpha = 0.95f)
    val unselectedColor = Color(0xFFB0BEC5)

    val itemColors = mapOf(
        BottomNavItem.Tasks.route to Color(0xFF2196F3),
        BottomNavItem.Calendar.route to Color(0xFFFF9800),
        BottomNavItem.Done.route to Color(0xFF4CAF50),
        BottomNavItem.Pomodoro.route to Color(0xFFF44336),
        BottomNavItem.Profile.route to Color(0xFF9C27B0)
    )

    NavigationBar(
        containerColor = backgroundColor,
        tonalElevation = 10.dp,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .height(70.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            val color = itemColors[item.route] ?: Color.Black

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) color else unselectedColor
                    )
                },
                label = {
                    Text(
                        item.title,
                        color = if (selected) color else unselectedColor
                    )
                },
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = color.copy(alpha = 0.15f)
                )
            )
        }
    }
}

