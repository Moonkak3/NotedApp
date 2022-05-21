package com.example.notedapp.ui.tasks

import android.content.Context
import android.content.res.Configuration
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

class TagsSpinnerAdapter(private val context: Context, private val unusedTags: ArrayList<Tag>) :
    BaseAdapter() {

    override fun getCount(): Int {
        return unusedTags.size
    }

    override fun getItem(p0: Int): Tag {
        return unusedTags[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(pos: Int, view: View?, viewGroup: ViewGroup?): View {
        return if (view == null) {
            val newView =
                LayoutInflater.from(context).inflate(R.layout.tags_spinner_layout, viewGroup, false)

            val tagName = newView.findViewById<TextView>(R.id.tagName)
            tagName.text = unusedTags[pos].name

            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    tagName.setTextColor(ContextCompat.getColor(context, R.color.lightGrey))
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    tagName.setTextColor(ContextCompat.getColor(context, R.color.darkGrey))
                }
            }

            val tagCircle = newView.findViewById<ImageView>(R.id.tagCircle)
            tagCircle.setColorFilter(Color.parseColor(unusedTags[pos].color))

            newView
        } else {
            view
        }
    }
}