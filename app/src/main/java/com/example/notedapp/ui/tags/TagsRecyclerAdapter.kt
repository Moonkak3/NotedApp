package com.example.notedapp.ui.tags

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.notedapp.MainViewModel
import com.example.notedapp.R
import com.example.notedapp.models.Tag

class TagsRecyclerAdapter(private val tags: ArrayList<Tag>, private val viewModel: MainViewModel) :
    RecyclerView.Adapter<TagsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.tag_card_layout, parent, false)
        return ViewHolder(v, tags, viewModel, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(tags[position])
    }

    override fun getItemCount() = tags.size

    // The class holding the list view
    class ViewHolder(
        itemView: View,
        tags: ArrayList<Tag>,
        viewModel: MainViewModel,
        adapter: TagsRecyclerAdapter
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val tagName: TextView = itemView.findViewById(R.id.tagName)
        private val numTasks: TextView = itemView.findViewById(R.id.numTasksTV)
        private val cardView: CardView = itemView.findViewById(R.id.card_view)

        init {
            itemView.setOnClickListener { view ->
                val tag = tags[adapterPosition]
                viewModel.setCurrTag(tag)
                view.context.startActivity(Intent(view.context, EditTagActivity::class.java))
            }
        }

        fun bindItems(tag: Tag) {
            tagName.text = tag.name
            numTasks.text = tag.numTasks.toString()
            cardView.setCardBackgroundColor(Color.parseColor(tag.color))
        }
    }
}