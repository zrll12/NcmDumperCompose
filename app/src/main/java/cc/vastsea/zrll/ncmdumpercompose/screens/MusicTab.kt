package cc.vastsea.zrll.ncmdumpercompose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cc.vastsea.zrll.ncmdumpercompose.data.PreferencesManager
import cc.vastsea.zrll.ncmdumpercompose.model.NcmFile
import cc.vastsea.zrll.ncmdumpercompose.utils.FileUtils
import cc.vastsea.zrll.ncmdumpercompose.utils.FormatUtils

class MusicTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Music"
            val icon = rememberVectorPainter(Icons.Default.MusicNote)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

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

        LaunchedEffect(inputDir) {
            if (!inputDir.isNullOrEmpty()) {
                val files = FileUtils.fetchNcmFileUri(context, inputDir!!.toUri())
                files.collect { fileList ->
                    ncmFiles = fileList
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索音乐文件") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredFiles) { filePath ->
                    NcmFileItem(filePath)
                }
            }
        }
    }


    @Composable
    private fun NcmFileItem(file: NcmFile) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
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