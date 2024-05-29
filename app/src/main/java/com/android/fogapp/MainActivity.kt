package com.android.fogapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.android.fogapp.ui.main.MainScreen
import com.android.fogapp.ui.theme.FogAppTheme
import com.android.fogapp.util.PermissionUtil
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException
import java.io.InputStream


/**
 * The main activity of the app
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            FogAppTheme {
                val navController = rememberNavController()
                MainScreen(navController = navController)
            }
        }
    }
}

