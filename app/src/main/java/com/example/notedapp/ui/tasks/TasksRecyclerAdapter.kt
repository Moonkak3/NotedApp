package com.example.notedapp.ui.tasks

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notedapp.MainViewModel
import com.example.notedapp.R
import com.example.notedapp.models.Tag
import com.example.notedapp.models.Task
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@SuppressLint("NotifyDataSetChanged")
class TasksRecyclerAdapter(
    private val tasks: ArrayList<Task>,
    private val viewModel: MainViewModel
) :
    RecyclerView.Adapter<TasksRecyclerAdapter.ViewHolder>() {

    private val TAG = "TasksRecyclerAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_card_layout, parent, false)
        return ViewHolder(v, tasks, viewModel, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(tasks[position], viewModel)
    }

    override fun getItemCount() = tasks.size

    // The class holding the list view
    class ViewHolder(
        itemView: View,
        tasks: ArrayList<Task>,
        viewModel: MainViewModel,
        adapter: TasksRecyclerAdapter
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val constraintLayout: ConstraintLayout =
            itemView.findViewById(R.id.constraintLayoutOuter)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        private val taskName: TextView = itemView.findViewById(R.id.taskName)
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        private val daysLeft: TextView = itemView.findViewById(R.id.daysLeftTV)
        private val days: TextView = itemView.findViewById(R.id.daysTV)

        init {
            itemView.setOnClickListener { view ->
                viewModel.setCurrTask(tasks[adapterPosition])
                view.context.startActivity(Intent(view.context, EditTaskActivity::class.java))
            }
        }

        fun bindItems(task: Task, viewModel: MainViewModel) {
            taskName.text = task.name
            if (task.timeDue != 0L) {
                constraintLayout.visibility = View.VISIBLE
//                daysLeft.visibility = View.VISIBLE
//                days.visibility = View.VISIBLE
                val timeLeft = task.timeDue - System.currentTimeMillis()
                val daysLeftInt = ceil((((timeLeft / 1000.0) / 60) / 60) / 24).toInt()
                daysLeft.text = daysLeftInt.toString()
                val daysLeftPercent = max(min(1f, daysLeftInt / 7f), 0f)
                daysLeft.setTextColor(
                    ColorUtils.blendARGB(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.alarmRed
                        ),
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.calmGreen
                        ),
                        daysLeftPercent
                    )
                )
                days.setTextColor(
                    ColorUtils.blendARGB(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.alarmRed
                        ),
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.calmGreen
                        ),
                        daysLeftPercent
                    )
                )
            } else {
                constraintLayout.visibility = View.GONE
            }

            val allTags = ArrayList<Tag>()
            val usedTags = ArrayList<Tag>()

            recyclerView.adapter = TagsRecyclerAdapter(usedTags, null, null)
            val layoutManager = LinearLayoutManager(itemView.context)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.layoutManager = layoutManager

            Firebase.database.reference.child("users").child(viewModel.getCurrUserEmail())
                .child("tags").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        allTags.clear()
                        for (snapshot in dataSnapshot.children) {
                            allTags.add(snapshot.getValue(Tag::class.java)!!)
                        }

                        usedTags.clear()
                        for (tag in allTags) {
                            for (tagKey in task.tagKeys) {
                                if (tag.key == tagKey) {
                                    usedTags.add(tag)
                                }
                            }
                        }
                        recyclerView.adapter!!.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            checkBox.isChecked = task.done
            checkBox.setOnClickListener {
                task.done = checkBox.isChecked
                viewModel.checkTask(task, checkBox.isChecked)
            }
        }
    }
}
