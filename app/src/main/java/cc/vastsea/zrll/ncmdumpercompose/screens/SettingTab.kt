package cc.vastsea.zrll.ncmdumpercompose.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.vastsea.zrll.ncmdumpercompose.data.PreferencesManager
import cc.vastsea.zrll.ncmdumpercompose.utils.FormatUtils
import io.github.hristogochev.vortex.tab.Tab
import kotlinx.coroutines.launch

class SettingTab : Tab {
    override val index: UInt = 2u

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val preferencesManager = remember { PreferencesManager(context) }

        val inputDir by preferencesManager.inputDirFlow.collectAsState(initial = "")
        val outputDir by preferencesManager.outputDirFlow.collectAsState(initial = "")

        val inputDirLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
            onResult = { uri ->
                uri?.let {
                    scope.launch {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        preferencesManager.saveInputDir(it.toString())
                    }
                }
            }
        )

        val outputDirLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
            onResult = { uri ->
                uri?.let {
                    scope.launch {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        preferencesManager.saveOutputDir(it.toString())
                    }
                }
            }
        )


        Column {
            Surface {
                Column {
                    Text(
                        "目录设置",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                    SettingItem(
                        title = "输入目录",
                        subtitle = FormatUtils.formatPath(inputDir),
                        onClick = { inputDirLauncher.launch(null) }
                    )
                    SettingItem(
                        title = "输出目录",
                        subtitle = FormatUtils.formatPath(outputDir),
                        onClick = { outputDirLauncher.launch(null) }
                    )

                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )

                    Text(
                        "调试选项",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }


    @Composable
    private fun SettingItem(
        title: String,
        subtitle: String = "",
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!subtitle.isEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.StartEllipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}