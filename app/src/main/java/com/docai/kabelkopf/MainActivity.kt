package com.docai.kabelkopf

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.docai.kabelkopf.ui.navigation.AppNavGraph
import com.docai.kabelkopf.ui.navigation.Screen
import com.docai.kabelkopf.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("first_launch", true)
        if (isFirstLaunch) {
            prefs.edit().putBoolean("first_launch", false).apply()
        }

        val startDestination = if (isFirstLaunch) Screen.Welcome.route else Screen.Home.route

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
