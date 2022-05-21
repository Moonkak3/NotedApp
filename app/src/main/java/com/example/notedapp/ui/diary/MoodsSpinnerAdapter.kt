package com.example.notedapp.ui.diary

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.example.notedapp.R
import com.example.notedapp.models.Mood

class MoodsSpinnerAdapter(private val context: Context, private val moods: Array<Mood>) :
    BaseAdapter() {

    override fun getCount(): Int {
        return moods.size
    }

    override fun getItem(p0: Int): Mood {
        return moods[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(pos: Int, view: View?, viewGroup: ViewGroup?): View {
        return if (view == null) {
            val newView =
                LayoutInflater.from(context).inflate(R.layout.tags_spinner_layout, viewGroup, false)

            val tagName = newView.findViewById<TextView>(R.id.tagName)
            tagName.text = moods[pos].toString()

            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    tagName.setTextColor(ContextCompat.getColor(context, R.color.lightGrey))
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    tagName.setTextColor(ContextCompat.getColor(context, R.color.darkGrey))
                }
            }

            val tagCircle = newView.findViewById<ImageView>(R.id.tagCircle)
            tagCircle.setImageDrawable(
                when (moods[pos]) {
                    Mood.ANGRY ->
                        AppCompatResources.getDrawable(context, R.drawable.gradient_red)
                    Mood.CONTENT ->
                        AppCompatResources.getDrawable(context, R.drawable.gradient_green)
                    Mood.CHEERFUL ->
                        AppCompatResources.getDrawable(context, R.drawable.gradient_yellow)
                    Mood.SAD ->
                        AppCompatResources.getDrawable(context, R.drawable.gradient_blue)
                }
            )

            newView
        } else {
            view
        }
    }
}