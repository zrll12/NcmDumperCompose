package cc.vastsea.zrll.ncmdumpercompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import cc.vastsea.zrll.ncmdumpercompose.screens.HomeScreen
import cc.vastsea.zrll.ncmdumpercompose.ui.theme.NcmDumperComposeTheme
import io.github.hristogochev.vortex.navigator.Navigator
import io.github.hristogochev.vortex.screen.CurrentScreen
import io.github.hristogochev.vortex.transitions.FadeTransition
import io.github.hristogochev.vortex.transitions.SlideTransition

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            NcmDumperComposeTheme {
                Navigator(HomeScreen()) { navigator ->
                    CurrentScreen(
                        navigator = navigator,
                        defaultOnScreenAppearTransition = SlideTransition.Horizontal.Appear,
                        defaultOnScreenDisappearTransition = SlideTransition.Horizontal.Disappear,
                    )
                }
            }
        }
    }
}