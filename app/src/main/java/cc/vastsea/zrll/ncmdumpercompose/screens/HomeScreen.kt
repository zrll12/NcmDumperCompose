package cc.vastsea.zrll.ncmdumpercompose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        TabNavigator(HomeTab()) { navigator ->
            Scaffold(
                topBar = { TopAppBar(title = { Text(navigator.current.options.title) }) },
                bottomBar = {
                    NavigationBar {
                        @Composable
                        fun NavigationBarTab(tab: Tab) {
                            print(tab.options.title)

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = tab.options.icon!!,
                                        contentDescription = tab.options.title
                                    )
                                },
                                label = { Text(tab.options.title) },
                                selected = navigator.current.options.title == tab.options.title,
                                onClick = { navigator.current = tab },
                            )
                        }


                        NavigationBarTab(HomeTab())
                        NavigationBarTab(MusicTab())
                        NavigationBarTab(SettingTab())
                    }
                }
            ) {
                Column(modifier = Modifier.padding(it)) {
                    CurrentTab()
                }
            }
        }
    }
}