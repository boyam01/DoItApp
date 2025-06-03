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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue




class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).taskDao()

    private val messages = listOf(
        "做得好！持續進步 💪",
        "完成一件事就是贏了一次 ✨",
        "又一項任務達成了，太讚啦！🔥"
    )

    var focusCount by mutableStateOf(0)
        private set

    fun resetFocusCount() {
        focusCount = 0
    }

    fun clearAllTasks() = viewModelScope.launch {
        dao.deleteAllTasks()
    }




    private val _motivation = MutableStateFlow<String?>(null)
    val motivation: StateFlow<String?> = _motivation

    fun clearMotivation() {
        _motivation.value = null
    }

    val tasks = dao.getAllTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun addTask(task: Task) = viewModelScope.launch {
        dao.insertTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        dao.deleteTask(task)
    }

    fun toggleTaskDone(task: Task) = viewModelScope.launch {
        val updated = task.copy(isDone = !task.isDone)
        dao.insertTask(updated)

        // ✅ 如果這次是從未完成 → 完成，顯示一句激勵語
        if (updated.isDone) {
            _motivation.value = messages.random()
        }
    }


    fun getTasksByDate(date: String): List<Task> {
        return tasks.value.filter { it.reminderDate == date }
    }
}