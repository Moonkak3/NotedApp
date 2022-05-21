package com.example.notedapp.models

import kotlin.collections.ArrayList

data class Task(
    val name: String = "Default Name",
    val description: String = "Default Description",
    val timeDue: Long = -1,         // Epoch time
    val timeCreated: Long = -1,     // Epoch time
    val tagKeys: ArrayList<String> = ArrayList(),
    var done: Boolean = false,
    var key: String = ""
)