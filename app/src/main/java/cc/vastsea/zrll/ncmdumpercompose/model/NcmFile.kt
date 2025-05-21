package cc.vastsea.zrll.ncmdumpercompose.model

import android.net.Uri


data class NcmFile(
    val uri: Uri,
    val size: Long,
    val name: String,
    val lastModified: Long,
    val checkState: Boolean = false,
    val taskState: TaskState = TaskState.Default
)

sealed class TaskState {
    data object Default : TaskState()
    data object Wait : TaskState()
    data object Dumping : TaskState()
    data object Success : TaskState()
    data object Error : TaskState()
}