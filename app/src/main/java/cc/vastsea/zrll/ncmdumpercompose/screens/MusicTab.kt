package cc.vastsea.zrll.ncmdumpercompose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cc.vastsea.zrll.ncmdumpercompose.data.PreferencesManager
import cc.vastsea.zrll.ncmdumpercompose.model.NcmFile
import cc.vastsea.zrll.ncmdumpercompose.utils.FileUtils
import cc.vastsea.zrll.ncmdumpercompose.utils.FormatUtils
import io.github.hristogochev.vortex.navigator.LocalNavigator
import io.github.hristogochev.vortex.navigator.parentOrThrow
import io.github.hristogochev.vortex.tab.Tab
import io.github.hristogochev.vortex.util.currentOrThrow

class MusicTab : Tab {
    override val index: UInt = 1u

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val preferencesManager = remember { PreferencesManager(context) }
        val inputDir by preferencesManager.inputDirFlow.collectAsState(initial = "")
        var ncmFiles by remember { mutableStateOf<List<NcmFile>>(emptyList()) }
        var searchQuery by remember { mutableStateOf("") }

        val filteredFiles = remember(ncmFiles, searchQuery) {
            if (searchQuery.isEmpty()) {
                ncmFiles
            } else {
                ncmFiles.filter { file ->
                    file.name.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        val outputDir by preferencesManager.outputDirFlow.collectAsState(initial = "")
        val finalOutputDir = outputDir?.let { "$it/NcmDumped" } ?: ""

        LaunchedEffect(inputDir, finalOutputDir) {
            if (!inputDir.isNullOrEmpty() && finalOutputDir.isNotEmpty()) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val files = FileUtils.fetchNcmFileUri(context, inputDir!!.toUri(), outputDir!!.toUri())
                    files.collect { fileList ->
                        ncmFiles = fileList
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索音乐文件") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                singleLine = true,
            )

            if (inputDir.isNullOrEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "没有设置输入目录",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = filteredFiles,
                    key = { it.name },
                    contentType = { "ncm_file" }
                ) { filePath ->
                    NcmFileItem(filePath, Modifier.animateItem())
                }
            }
        }
    }


    @Composable
    private fun NcmFileItem(file: NcmFile, modifier: Modifier) {
        val navigator = LocalNavigator.currentOrThrow.parentOrThrow

        Card(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = file.taskState.getBackgroundColor())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = { navigator.push(MusicDetailScreen(file)) })
                    .padding(16.dp)
            ) {
                Text(
                    text = FormatUtils.formatNcmName(file.name).second,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${FormatUtils.formatNcmName(file.name).first}・${
                        FormatUtils.formatSize(
                            file.size
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}