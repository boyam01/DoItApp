package com.yourname.doitapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val isDone: Boolean = false,
    val reminderDate: String? = null,  // yyyy-MM-dd，用於日曆同步
    val subtasks: List<String> = emptyList(),  // 子任務
    val timestamp: Long = System.currentTimeMillis()  // 建立時間，用於排序
)
