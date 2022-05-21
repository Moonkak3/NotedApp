package com.example.notedapp.ui.tasks

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.notedapp.MainViewModel
import com.example.notedapp.R
import com.example.notedapp.models.Tag
import com.google.android.material.snackbar.Snackbar


class TagsRecyclerAdapter(
    private val usedTags: ArrayList<Tag>,
    private val unusedTags: ArrayList<Tag>?,
    private val spinner: Spinner?
) :
    RecyclerView.Adapter<TagsRecyclerAdapter.ViewHolder>() {

    private lateinit var holder: ViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = if (unusedTags == null) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.tags_card_layout_small, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.tags_card_layout, parent, false)
        }
        return ViewHolder(v, usedTags, unusedTags, spinner, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(usedTags[position])
    }

    override fun getItemCount() = usedTags.size

    // The class holding the list view
    class ViewHolder(
        itemView: View,
        usedTags: ArrayList<Tag>,
        unusedTags: ArrayList<Tag>?,
        spinner: Spinner?,
        adapter: TagsRecyclerAdapter
    ) : RecyclerView.ViewHolder(itemView) {
        private var layout: LinearLayout = itemView.findViewById(R.id.linearLayout)
        private var tagCircle: ImageView = itemView.findViewById(R.id.tagCircle)
        private var tagName: TextView = itemView.findViewById(R.id.tagName)

        init {
            if (unusedTags != null && spinner != null) {
                itemView.setOnClickListener { view ->
                    Snackbar.make(
                        view,
                        "Remove ${usedTags[adapterPosition].name} tag?",
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction("REMOVE") {
                            unusedTags.add(usedTags[adapterPosition])
                            usedTags.removeAt(adapterPosition)
                            adapter.notifyItemRemoved(adapterPosition)
                            adapter.notifyItemRangeChanged(adapterPosition, usedTags.size)
                            spinner.visibility = View.VISIBLE

                        }.show()
                }
            }
        }

        fun bindItems(tag: Tag) {
            layout.background.setTint(Color.parseColor(tag.color))
            tagCircle.setColorFilter(Color.parseColor(tag.color))
            tagName.text = tag.name
        }
    }
}
