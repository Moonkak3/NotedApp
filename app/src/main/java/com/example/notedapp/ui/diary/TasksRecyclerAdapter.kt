package com.example.notedapp.ui.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.notedapp.R
import com.example.notedapp.models.Task
import com.google.android.material.snackbar.Snackbar


class TasksRecyclerAdapter(
    private val usedTasks: ArrayList<Task>,
    private val unusedTasks: ArrayList<Task>?,
    private val spinner: Spinner?
) :
    RecyclerView.Adapter<TasksRecyclerAdapter.ViewHolder>() {

    private lateinit var holder: ViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = if (unusedTasks == null) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.tags_card_layout_small, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.tags_card_layout, parent, false)
        }
        return ViewHolder(v, usedTasks, unusedTasks, spinner, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(usedTasks[position])
    }

    override fun getItemCount() = usedTasks.size

    // The class holding the list view
    class ViewHolder(
        itemView: View,
        usedTasks: ArrayList<Task>,
        unusedTasks: ArrayList<Task>?,
        spinner: Spinner?,
        adapter: TasksRecyclerAdapter
    ) : RecyclerView.ViewHolder(itemView) {
        private var layout: LinearLayout = itemView.findViewById(R.id.linearLayout)
        private var tagCircle: ImageView = itemView.findViewById(R.id.tagCircle)
        private var tagName: TextView = itemView.findViewById(R.id.tagName)

        init {
            if (unusedTasks != null && spinner != null) {
                itemView.setOnClickListener { view ->
                    Snackbar.make(
                        view,
                        "Remove ${usedTasks[adapterPosition].name} task?",
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction("REMOVE") {
                            unusedTasks.add(usedTasks[adapterPosition])
                            usedTasks.removeAt(adapterPosition)
                            adapter.notifyItemRemoved(adapterPosition)
                            adapter.notifyItemRangeChanged(adapterPosition, usedTasks.size)
                            Toast.makeText(view.context, "Task removed", Toast.LENGTH_SHORT).show()
                            spinner.visibility = View.VISIBLE

                        }.show()
                }
            }
        }

        fun bindItems(task: Task) {
            layout.background.setTint(ContextCompat.getColor(itemView.context, R.color.secondaryColor))
            tagCircle.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondaryColor))
            tagName.text = task.name
        }
    }
}
