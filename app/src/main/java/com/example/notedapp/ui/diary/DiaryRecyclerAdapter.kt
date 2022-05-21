package com.example.notedapp.ui.diary


import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.notedapp.MainViewModel
import com.example.notedapp.R
import com.example.notedapp.models.DiaryEntry
import com.example.notedapp.models.Mood
import com.example.notedapp.models.Task
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class DiaryRecyclerAdapter(
    private val entries: ArrayList<DiaryEntry>,
    private val viewModel: MainViewModel
) :
    RecyclerView.Adapter<DiaryRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.diary_card_layout, parent, false)
        return ViewHolder(v, entries, viewModel, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(entries[position], viewModel)
    }

    override fun getItemCount() = entries.size

    // The class holding the list view
    class ViewHolder(
        itemView: View,
        entries: ArrayList<DiaryEntry>,
        viewModel: MainViewModel,
        adapter: DiaryRecyclerAdapter
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val entryTitle: TextView = itemView.findViewById(R.id.entry_title)
        private val entryDate: TextView = itemView.findViewById(R.id.entry_date)
        private val entryDescription: TextView = itemView.findViewById(R.id.entry_description)
        private val constraintLayoutOuter: ConstraintLayout =
            itemView.findViewById(R.id.constraintLayoutOuter)
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)

        init {
            itemView.setOnClickListener { view ->
                viewModel.setCurrEntry(entries[adapterPosition])
                view.context.startActivity(Intent(view.context, EditDiaryEntryActivity::class.java))
            }
        }

        fun bindItems(entry: DiaryEntry, viewModel: MainViewModel) {
            entryTitle.text = entry.title
            entryDate.text =
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(entry.date))
                    .toString()
            entryDescription.text = entry.description
            constraintLayoutOuter.background = when (entry.mood) {
                Mood.ANGRY ->
                    AppCompatResources.getDrawable(itemView.context, R.drawable.gradient_red)
                Mood.CONTENT ->
                    AppCompatResources.getDrawable(itemView.context, R.drawable.gradient_green)
                Mood.CHEERFUL ->
                    AppCompatResources.getDrawable(itemView.context, R.drawable.gradient_yellow)
                Mood.SAD ->
                    AppCompatResources.getDrawable(itemView.context, R.drawable.gradient_blue)
            }

            val allTasks = ArrayList<Task>()
            val usedTasks = ArrayList<Task>()

            recyclerView.adapter = TasksRecyclerAdapter(usedTasks, null, null)
            recyclerView.layoutManager = FlexboxLayoutManager(itemView.context)

            Firebase.database.reference.child("users").child(viewModel.getCurrUserEmail()).child("tasks").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    allTasks.clear()
                    for (snapshot in dataSnapshot.children) {
                        allTasks.add(snapshot.getValue(Task::class.java)!!)
                    }

                    usedTasks.clear()
                    for (task in allTasks){
                        for (taskKey in entry.taskKeys){
                            if (task.key == taskKey) {
                                usedTasks.add(task)
                            }
                        }
                    }
                    recyclerView.adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }
}