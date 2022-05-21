package com.example.notedapp.ui.tags

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.notedapp.MainViewModel
import com.example.notedapp.R
import com.example.notedapp.databinding.ActivityEditTagBinding
import com.example.notedapp.models.Tag
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import java.util.*


class EditTagActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTagBinding
    private val viewModel by viewModels<MainViewModel>()
    private val TAG = "EditTagActivityTag"

    private var currTag: Tag? = null
    private var color = -1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTagBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currTag = viewModel.getCurrTag()

        if (currTag != null) {
            updateColor(Color.parseColor(currTag!!.color))
            binding.tagName.setText(currTag!!.name)
        } else {
            updateColor(getColor(R.color.primaryColor))
        }

        binding.colorPickerBTN.setOnClickListener {
            ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .noSliders()
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener { selectedColor ->
                }
                .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                    updateColor(selectedColor)
                }
                .setNegativeButton("cancel") { dialog, which ->

                }
                .build()
                .show()
        }

        binding.fab.setOnClickListener {

            if (binding.tagName.text.isBlank()) {
                Toast.makeText(applicationContext, "Please add a tag name", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val tag = Tag(
                binding.tagName.text.toString(),
                String.format("#%06X", (0xFFFFFF and color)),
                if (currTag != null) currTag!!.numTasks else 0,
                if (currTag != null) currTag!!.key else ""
            )
            viewModel.addTag(tag).addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "${tag.name} added to tags",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            finish()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.i(TAG, menu.toString())
        menuInflater.inflate(R.menu.delete, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                if (currTag != null) {

                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setCancelable(true)
                    builder.setTitle("Are you sure you want to delete this?")
                    builder.setMessage("Deleted data cannot be recovered")
                    builder.setPositiveButton("Delete") { dialog, which ->
                        deleteCurrTag()
                    }
                    builder.setNegativeButton(android.R.string.cancel) { dialog, which ->

                    }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()

                } else {
                    Toast.makeText(
                        applicationContext,
                        "You need to add this task before deleting it",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
            else -> {}
        }

        return false
    }

    private fun deleteCurrTag() {
        if (currTag != null) {
            viewModel.deleteTag(currTag!!).addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "${currTag!!.name} deleted from tags",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        finish()
    }

    private fun updateColor(newColor: Int = color) {
        this.color = newColor
        binding.coordinator.setBackgroundColor(color)
        binding.colorCircle.setColorFilter(color)
        binding.colorPickerBTN.setTextColor(color)
        binding.colorPickerBTN.iconTint = ColorStateList.valueOf(color)
    }

    override fun finish() {
        viewModel.setCurrTag(null)
        super.finish()
    }
}