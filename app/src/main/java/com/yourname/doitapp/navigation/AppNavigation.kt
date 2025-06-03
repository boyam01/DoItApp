package com.yourname.doitapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yourname.doitapp.screens.*

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
            NavigationBar {
                val navBackStackEntry = navController.currentBackStackEntryAsState().value
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = { navController.navigate(item.route) }
                    )
                }
            }
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
            composable(BottomNavItem.Profile.route) { ProfileScreen() }
            composable(BottomNavItem.Pomodoro.route) { PomodoroScreen() }
        }
    }
}
