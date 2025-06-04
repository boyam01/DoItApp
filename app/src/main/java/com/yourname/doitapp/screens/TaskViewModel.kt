package com.yourname.doitapp.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.doitapp.data.AppDatabase
import com.yourname.doitapp.data.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val dao = db.taskDao()

    private val messages = listOf(
        "做得好！持續進步 💪",
        "完成一件事就是贏了一次 ✨",
        "又一項任務達成了，太讚啦！🔥"
    )

    var focusCount by mutableStateOf(0)
        private set

    private val _motivation = MutableStateFlow<String?>(null)
    val motivation: StateFlow<String?> = _motivation

    val tasks = dao.getAllTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun resetFocusCount() {
        focusCount = 0
    }

    fun clearAllTasks() = viewModelScope.launch {
        dao.deleteAllTasks()
    }

    fun addTask(task: Task) = viewModelScope.launch {
        dao.insertTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        dao.deleteTask(task)
    }

    fun toggleTaskDone(task: Task) = viewModelScope.launch {
        val updated = task.copy(isDone = !task.isDone)
        dao.insertTask(updated)
        if (updated.isDone) {
            _motivation.value = messages.random()
        }
    }

    fun getTasksByDate(date: String): List<Task> {
        val formatterFull = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        val formatterDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        return tasks.value.filter { task ->
            task.reminderDate?.let {
                try {
                    val parsedDate = formatterFull.parse(it)
                    val formatted = formatterDate.format(parsedDate!!)
                    formatted == date
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }
    }


    fun updateSubtaskState(taskId: Int, index: Int, newState: Boolean) {
        viewModelScope.launch {
            val task = dao.getById(taskId) ?: return@launch
            val newStates = task.subtaskStates.toMutableList().also { it[index] = newState }
            dao.update(task.copy(subtaskStates = newStates))
        }
    }

    fun updateSubtaskContent(taskId: Int, index: Int, newText: String) {
        viewModelScope.launch {
            val task = dao.getById(taskId) ?: return@launch
            val newSubtasks = task.subtasks.toMutableList().also { it[index] = newText }
            dao.update(task.copy(subtasks = newSubtasks))
        }
    }

    fun removeSubtask(taskId: Int, index: Int) {
        viewModelScope.launch {
            val task = dao.getById(taskId) ?: return@launch
            val newSubtasks = task.subtasks.toMutableList().also { it.removeAt(index) }
            val newStates = task.subtaskStates.toMutableList().also { it.removeAt(index) }
            dao.update(task.copy(subtasks = newSubtasks, subtaskStates = newStates))
        }
    }

    fun clearMotivation() {
        _motivation.value = null
    }
}
