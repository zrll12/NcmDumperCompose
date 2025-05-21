package cc.vastsea.zrll.ncmdumpercompose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.vastsea.zrll.ncmdumpercompose.data.PreferencesManager
import cc.vastsea.zrll.ncmdumpercompose.utils.FormatUtils
import io.github.hristogochev.vortex.navigator.LocalNavigator
import io.github.hristogochev.vortex.tab.Tab
import io.github.hristogochev.vortex.util.currentOrThrow

class HomeTab : Tab {
    override val index: UInt = 0u

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val preferencesManager = remember { PreferencesManager(context) }

        val tabNavigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            val inputDir by preferencesManager.inputDirFlow.collectAsState(initial = "")
            val outputDir by preferencesManager.outputDirFlow.collectAsState(initial = "")

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text("Overview", fontSize = 23.sp)
                    Spacer(Modifier.padding(5.dp))

                    Text("Input dir: ${FormatUtils.formatPath(inputDir)}")
                    Text("Output dir: ${FormatUtils.formatPath(outputDir)}")
                    Spacer(Modifier.padding(5.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = {
                            tabNavigator.current = SettingTab()
                        }) {
                            Text("Set dirs")
                        }
                    }
                }
            }
        }
    }
}