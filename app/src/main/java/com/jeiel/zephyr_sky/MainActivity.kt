package com.jeiel.zephyr_sky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeiel.zephyr_sky.ui.theme.MyApplicationTheme
import com.jeiel.zephyr_sky.ui.weather.DarkThemeSetting
import com.jeiel.zephyr_sky.ui.weather.WeatherScreen
import com.jeiel.zephyr_sky.ui.weather.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WeatherViewModel = viewModel()
            val context = LocalContext.current
            
            val darkThemeSetting by viewModel.darkThemeSetting.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (darkThemeSetting) {
                DarkThemeSetting.SYSTEM -> systemDark
                DarkThemeSetting.LIGHT -> false
                DarkThemeSetting.DARK -> true
            }

            LaunchedEffect(Unit) {
                viewModel.loadPreferences(context)
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    WeatherScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
