package cc.vastsea.zrll.ncmdumpercompose.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cc.vastsea.zrll.ncmdumpercompose.data.PreferencesManager
import cc.vastsea.zrll.ncmdumpercompose.model.NcmFile
import cc.vastsea.zrll.ncmdumpercompose.model.TaskState
import cc.vastsea.zrll.ncmdumpercompose.utils.FileUtils
import cc.vastsea.zrll.ncmdumpercompose.utils.FormatUtils
import io.github.hristogochev.vortex.navigator.LocalNavigator
import io.github.hristogochev.vortex.tab.Tab
import io.github.hristogochev.vortex.util.currentOrThrow

class MusicTab : Tab {
    override val index: UInt = 1u

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val preferencesManager = remember { PreferencesManager(context) }
        val inputDir by preferencesManager.inputDirFlow.collectAsState(initial = "")
        var ncmFiles by remember { mutableStateOf<List<NcmFile>>(emptyList()) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedFiles by remember { mutableStateOf<Set<String>>(emptySet()) }
        var isSelectionMode by remember { mutableStateOf(false) }

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
            if (!inputDir.isNullOrEmpty()) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val files = if (finalOutputDir.isNotEmpty()) {
                        FileUtils.fetchNcmFileUri(context, inputDir!!.toUri(), outputDir!!.toUri())
                    } else {
                        FileUtils.fetchNcmFileUri(context, inputDir!!.toUri(), null)
                    }
                    files.collect { fileList ->
                        ncmFiles = fileList
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("搜索音乐文件") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                    singleLine = true,
                )

                if (isSelectionMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = {
                            val unconvertedFiles = filteredFiles
                                .filter { it.taskState == TaskState.Wait }
                                .map { it.name }
                            selectedFiles = if (selectedFiles.size == unconvertedFiles.size) {
                                emptySet()
                            } else {
                                unconvertedFiles.toSet()
                            }
                        }) {
                            Icon(
                                if (selectedFiles.size == filteredFiles.count { it.taskState == TaskState.Wait })
                                    Icons.Default.CheckBoxOutlineBlank
                                else
                                    Icons.Default.SelectAll,
                                contentDescription = "全选未转换"
                            )
                        }
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedFiles = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "取消选择")
                        }
                        IconButton(onClick = {
                            // TODO: 实现批量转换功能
                            selectedFiles.forEach { fileName ->
                                // 处理选中的文件
                            }
                            isSelectionMode = false
                            selectedFiles = emptySet()
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "转换选中")
                        }
                    }
                }
            }

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
                    NcmFileItem(
                        file = filePath,
                        modifier = Modifier.animateItem(),
                        first = filePath == filteredFiles.first(),
                        last = filePath == filteredFiles.last(),
                        isSelected = selectedFiles.contains(filePath.name),
                        isSelectionMode = isSelectionMode,
                        onLongClick = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedFiles = setOf(filePath.name)
                            }
                        },
                        onClick = {
                            if (isSelectionMode) {
                                selectedFiles = if (selectedFiles.contains(filePath.name)) {
                                    selectedFiles - filePath.name
                                } else {
                                    selectedFiles + filePath.name
                                }
                            } else {
                                navigator.push(MusicDetailScreen(filePath))
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun NcmFileItem(
        file: NcmFile,
        modifier: Modifier,
        first: Boolean = false,
        last: Boolean = false,
        isSelected: Boolean = false,
        isSelectionMode: Boolean = false,
        onLongClick: () -> Unit = {},
        onClick: () -> Unit = {}
    ) {
        val isDarkTheme = isSystemInDarkTheme()
        val backgroundTargetColor =
            if (isSelected) file.taskState.getSelectedBackgroundColor()
            else file.taskState.getBackgroundColor()

        val textTargetColor =
            if (isDarkTheme) MaterialTheme.colorScheme.onSurface
            else {
                if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            }

        val backgroundColor by animateColorAsState(
            targetValue = backgroundTargetColor,
            animationSpec = tween(durationMillis = 200),
            label = "backgroundColor"
        )

        val textColor by animateColorAsState(
            targetValue = textTargetColor,
            animationSpec = tween(durationMillis = 200),
            label = "textColor"
        )

        Card(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp, vertical = 3.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongClick() },
                        onTap = { onClick() }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = textColor
            ),
            shape = RoundedCornerShape(
                topStart = if (first) 15.dp else 8.dp,
                topEnd = if (first) 15.dp else 8.dp,
                bottomStart = if (last) 15.dp else 8.dp,
                bottomEnd = if (last) 15.dp else 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = FormatUtils.formatNcmName(file.name).second,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${FormatUtils.formatNcmName(file.name).first}・${
                        FormatUtils.formatSize(file.size)
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}