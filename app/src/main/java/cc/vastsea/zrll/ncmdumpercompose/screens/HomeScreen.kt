package cc.vastsea.zrll.ncmdumpercompose.screens

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import io.github.hristogochev.vortex.navigator.LocalNavigator
import io.github.hristogochev.vortex.navigator.Navigator
import io.github.hristogochev.vortex.screen.Screen
import io.github.hristogochev.vortex.tab.CurrentTab
import io.github.hristogochev.vortex.tab.Tab
import io.github.hristogochev.vortex.transitions.FadeTransition
import io.github.hristogochev.vortex.util.currentOrThrow

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        Navigator(HomeTab()) { navigator ->
            Scaffold(
                topBar = { TopAppBar(title = { Text("Ncm Dumper Compose") }) },
                content = {
                    CurrentTab(
                        navigator, modifier = Modifier.padding(it),
                        defaultOnScreenAppearTransition = FadeTransition,
                        defaultOnScreenDisappearTransition = FadeTransition,
                    )
                },
                bottomBar = {
                    NavigationBar {
                        TabNavigationItem(HomeTab())
                        TabNavigationItem(MusicTab())
                        TabNavigationItem(SettingTab())
                    }
                }
            )
        }
    }


    @Composable
    private fun RowScope.TabNavigationItem(tab: Tab) {
        val navigator = LocalNavigator.currentOrThrow

        val icon = when (tab.index) {
            0u -> rememberVectorPainter(Icons.Default.Home)
            1u -> rememberVectorPainter(Icons.Default.MusicNote)
            2u -> rememberVectorPainter(Icons.Default.Settings)
            else -> return
        }

        val title = when (tab.index) {
            0u -> "Home"
            1u -> "Music"
            2u -> "Settings"
            else -> return
        }

        NavigationBarItem(
            selected = navigator.current.key == tab.key,
            onClick = { navigator.current = tab },
            icon = { Icon(painter = icon, contentDescription = title) },
            label = { Text(title) },
            alwaysShowLabel = false
        )
    }

}