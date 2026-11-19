package com.echeo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.echeo.app.ui.MainScreen
import com.echeo.app.ui.MainViewModel
import com.echeo.app.ui.theme.EcheoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.factory(application as EcheoApp) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterFullScreen()
        setContent {
            EcheoTheme { MainScreen(viewModel) }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDate()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterFullScreen()
    }

    /** Plein écran automatique : barres système masquées, réapparaissent d'un glissement. */
    private fun enterFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
