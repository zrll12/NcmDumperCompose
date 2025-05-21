package cc.vastsea.zrll.ncmdumpercompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import cc.vastsea.zrll.ncmdumpercompose.screens.HomeScreen
import cc.vastsea.zrll.ncmdumpercompose.ui.theme.NcmDumperComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            NcmDumperComposeTheme {
                Navigator(HomeScreen()) {
                    SlideTransition(it)
                }
            }
        }
    }
}