package com.example.notedapp.models

data class DiaryEntry(
    val title: String = "Default Title",
    val description: String = "Default Description",
    val date: Long = 0L,
    val taskKeys: ArrayList<String> = ArrayList(),
    val mood: Mood = Mood.CONTENT
)