package cc.vastsea.zrll.ncmdumpercompose

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cc.vastsea.zrll.ncmdumpercompose.pages.HomePage
import cc.vastsea.zrll.ncmdumpercompose.pages.MusicPage

class HomeScreen: Screen {
    @Composable
    override fun Content() {
        Scaffold {
            HomePage(it, onNavigateToSettings = {})
        }
    }
}

class MusicScreen: Screen {
    @Composable
    override fun Content() {
        Scaffold {
            MusicPage(it)
        }
    }
}