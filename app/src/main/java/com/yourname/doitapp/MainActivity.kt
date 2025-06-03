package com.yourname.doitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.yourname.doitapp.navigation.AppNavigation
import com.yourname.doitapp.ui.theme.DoItAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoItAppTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}