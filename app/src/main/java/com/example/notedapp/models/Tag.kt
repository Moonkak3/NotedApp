package com.example.notedapp.models

import com.google.firebase.database.Exclude


data class Tag(
    val name: String = "DefaultTag",
    val color: String = "#808080", // Hex code
    val numTasks: Int = 0,
    var key: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "color" to color,
            "numTasks" to numTasks,
            "key" to key,
        )
    }
}