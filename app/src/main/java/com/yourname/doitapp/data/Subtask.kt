package com.yourname.doitapp.data

import java.io.Serializable
import java.util.UUID

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    var isDone: Boolean = false
) : Serializable
