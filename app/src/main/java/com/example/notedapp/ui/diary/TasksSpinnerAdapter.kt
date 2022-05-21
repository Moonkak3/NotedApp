package com.example.notedapp.ui.diary

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.notedapp.R
import com.example.notedapp.models.Tag
import com.example.notedapp.models.Task

class TasksSpinnerAdapter (private val context: Context, private val unusedTasks: ArrayList<Task>) : BaseAdapter() {

    override fun getCount(): Int {
        return unusedTasks.size
    }

    override fun getItem(p0: Int): Task {
        return unusedTasks[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(pos: Int, view: View?, viewGroup: ViewGroup?): View {
        return if (view == null){
            val newView = LayoutInflater.from(context).inflate(R.layout.tags_spinner_layout, viewGroup, false)

            val tagName = newView.findViewById<TextView>(R.id.tagName)
            tagName.text = unusedTasks[pos].name
            val tagCircle = newView.findViewById<ImageView>(R.id.tagCircle)
            tagCircle.setColorFilter(ContextCompat.getColor(context, R.color.secondaryColor))

            newView
        } else {
            view
        }
    }
}