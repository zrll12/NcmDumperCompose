package cc.vastsea.zrll.ncmdumpercompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cc.vastsea.zrll.ncmdumpercompose.pages.HomePage
import cc.vastsea.zrll.ncmdumpercompose.pages.MusicPage
import cc.vastsea.zrll.ncmdumpercompose.pages.SettingPage
import cc.vastsea.zrll.ncmdumpercompose.ui.theme.NcmDumperComposeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            NcmDumperComposeTheme {
                val routes = listOf(
                    "home",
                    "musics",
                    "settings",
                )
                val routeIcons = listOf(
                    Icons.Outlined.Home to Icons.Filled.Home,
                    Icons.Outlined.MusicNote to Icons.Filled.MusicNote,
                    Icons.Outlined.Settings to Icons.Filled.Settings,
                )
                var current by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopAppBar(title = { Text("NCM Dumper Compose") }) },
                    bottomBar = { NavigationBar {
                        routeIcons.forEachIndexed { index, (icon, selectedIcon) ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (current == index) selectedIcon else icon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(routes[index]) },
                                selected = current == index,
                                onClick = { current = index }
                            )
                        }
                    }})
                { innerPadding ->
                    AnimatedContent(targetState = current, label = "pageTransition") { targetState ->
                    when (targetState) {
                        0 -> HomePage(
                            paddingValues = innerPadding,
                            onNavigateToSettings = { current = 2 }
                        )
                        1 -> MusicPage(innerPadding)
                        2 -> SettingPage(innerPadding)
                    }
                }
                }
            }
        }
    }
}