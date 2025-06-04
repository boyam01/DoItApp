package com.yourname.doitapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val isDone: Boolean = false,
    val reminderDate: String? = null,
    val subtasks: List<String> = emptyList(),
    val subtaskStates: List<Boolean> = emptyList(), // ✅ 不加註解！
    val timestamp: Long = System.currentTimeMillis()
)
