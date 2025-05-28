package cc.vastsea.zrll.ncmdumpercompose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.vastsea.zrll.ncmdumpercompose.data.PreferencesManager
import cc.vastsea.zrll.ncmdumpercompose.model.NcmFile
import cc.vastsea.zrll.ncmdumpercompose.utils.FileUtils
import cc.vastsea.zrll.ncmdumpercompose.utils.FormatUtils
import cc.vastsea.zrll.ncmdumpercompose.utils.NcmUtils
import io.github.hristogochev.vortex.navigator.LocalNavigator
import io.github.hristogochev.vortex.screen.Screen
import io.github.hristogochev.vortex.util.currentOrThrow
import kotlinx.coroutines.launch

data class MusicDetailScreen(val file: NcmFile) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var isConverting by remember { mutableStateOf(false) }
        val inputStream = FileUtils.getFileInputStream(context, file.uri)!!
        val metadata = NcmUtils.getNcmMetadata(inputStream)
        val artwork = NcmUtils.getNcmArtwork(inputStream)
        val preferencesManager = remember { PreferencesManager(context) }
        val outputDir by preferencesManager.outputDirFlow.collectAsState(initial = "")
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            FormatUtils.formatNcmName(file.name).second,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    })
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(20.dp)
            ) {
                Image(
                    bitmap = artwork!!.asImageBitmap(),
                    contentDescription = "Album Artwork",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    OutlinedButton(onClick = {}) { Text("网易云音乐打开") }
                    Spacer(Modifier.padding(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isConverting = true
                                NcmUtils.dumpNCM(
                                    inputStream = FileUtils.getFileInputStream(context, file.uri)!!,
                                    fileName = file.name,
                                    onSuccess = {
                                        isConverting = false
                                        navigator.pop()
                                    },
                                    onFailure = {
                                        isConverting = false
                                    },
                                    outputDir = FormatUtils.formatPath(outputDir),
                                    context = context
                                )
                            }
                        },
                        enabled = !isConverting && !outputDir.isNullOrEmpty()
                    ) {
                        Text(if (isConverting) "转换中..." else if (outputDir.isNullOrEmpty()) "请先设置输出目录" else "转换")
                    }
                }

                Text(
                    "Artist: ${metadata.artist}",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "Album: ${metadata.album}",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "File Size: ${FormatUtils.formatSize(file.size)}",
                    modifier = Modifier.padding(bottom = 15.dp)
                )
            }
        }
    }
}
