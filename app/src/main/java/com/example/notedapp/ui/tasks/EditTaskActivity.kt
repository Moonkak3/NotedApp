package com.example.notedapp.ui.tasks

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.notedapp.MainViewModel
import com.example.notedapp.R
import com.example.notedapp.databinding.ActivityEditTaskBinding
import com.example.notedapp.models.Tag
import com.example.notedapp.models.Task
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class EditTaskActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityEditTaskBinding
    private val viewModel by viewModels<MainViewModel>()
    private val TAG = "EditTaskActivityTag"
    private lateinit var database: DatabaseReference
    private val allTags = ArrayList<Tag>()


    private val usedTags: ArrayList<Tag> = ArrayList()
    private val unusedTags: ArrayList<Tag> = ArrayList()
    private var dueDate = 0L
    private var dueTime = 0L
    private var currTask: Task? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        database = Firebase.database.reference.child("users").child(viewModel.getCurrUserEmail()).child("tags")

        if (unusedTags.isEmpty()) {
            unusedTags.add(Tag("Loading tags..."))
        }

        currTask = viewModel.getCurrTask()

        if (currTask != null) {
            binding.taskName.setText(currTask!!.name)
            binding.taskDescription.setText(currTask!!.description)
            binding.checkbox.isChecked = currTask!!.done
            if (currTask!!.timeDue != 0L) {
                val dueDateTime = Date(currTask!!.timeDue)
                binding.datePickerBTN.text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDateTime)
                val date = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).parse(binding.datePickerBTN.text.toString())
                dueDate = date!!.time

                binding.timePickerBTN.text =
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(dueDateTime)
                val time = SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).parse(binding.timePickerBTN.text.toString())
                dueTime = time!!.time
            }
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allTags.clear()
                for (snapshot in dataSnapshot.children) {
                    allTags.add(snapshot.getValue(Tag::class.java)!!)
                }
                unusedTags.clear()
                unusedTags.add(Tag("Add tag"))

                usedTags.clear()

                if (currTask != null) {

                    for (tag in allTags){
                        for (tagKey in currTask!!.tagKeys){
                            if (tag.key == tagKey) {
                                usedTags.add(tag)
                            }
                        }
                    }

                    for (tag in allTags) {
                        var duplicate = false
                        for (usedTag in usedTags){
                            if (usedTag.key == tag.key) {
                                duplicate = true
                                break
                            }
                        }
                        if (!duplicate) {
                            unusedTags.add(tag)
                        }
                    }
                } else {
                    unusedTags.addAll(allTags)
                }
                if (unusedTags.size == 1) {
                    binding.tagsSpinner.visibility = View.GONE
                }
                binding.tagsSpinner.setSelection(0)
                binding.tagsSpinner.adapter = TagsSpinnerAdapter(baseContext, unusedTags)
                binding.recyclerView.adapter =
                    TagsRecyclerAdapter(usedTags, unusedTags, binding.tagsSpinner)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        binding.tagsSpinner.onItemSelectedListener = this
        binding.recyclerView.layoutManager = FlexboxLayoutManager(applicationContext)

        binding.datePickerBTN.setOnClickListener {
            val dialogDate = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, Y, M, D ->
                // M is 0 to 11 (0 indexed)
                binding.datePickerBTN.text = "$D/${M + 1}/$Y"
                val date = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).parse(binding.datePickerBTN.text.toString())
                dueDate = date!!.time
                Log.i(TAG, binding.datePickerBTN.text.toString())
                Log.i(TAG, Date(dueDate).toString())
            }
            val year = dialogDate.get(Calendar.YEAR)
            val month = dialogDate.get(Calendar.MONTH)
            val day = dialogDate.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, dateSetListener, year, month, day).show()
        }
        binding.removeDueDate.setOnClickListener {
            binding.datePickerBTN.text = getString(R.string.set_due_date)
            dueDate = 0L
        }

        binding.timePickerBTN.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                binding.timePickerBTN.text = formatTime(hour, minute)
                val time = SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).parse(binding.timePickerBTN.text.toString())
                dueTime = time!!.time
            }
            TimePickerDialog(this, timeSetListener, 0, 0, true).show()
        }
        binding.removeDueTime.setOnClickListener {
            binding.timePickerBTN.text = getString(R.string.set_due_time)
            dueTime = 0L
        }

        binding.fab.setOnClickListener {

            if (binding.taskName.text.isBlank()) {
                Toast.makeText(applicationContext, "Please add a task name", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (dueTime != 0L && dueDate == 0L) {
                val calendar = Calendar.getInstance()
                val Y = calendar.get(Calendar.YEAR)
                val M = calendar.get(Calendar.MONTH)
                val D = calendar.get(Calendar.DAY_OF_MONTH)
                binding.datePickerBTN.text = "$D/${M + 1}/$Y"
                val date = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).parse(binding.datePickerBTN.text.toString())
                dueDate = date!!.time
            } else if (dueDate != 0L && dueTime == 0L) {
                binding.timePickerBTN.text = "00:00"
                val date = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).parse(binding.datePickerBTN.text.toString())
                dueDate = date!!.time
            }
            val dueDateTime = if (!(dueDate==0L && dueTime==0L)){
                SimpleDateFormat("dd/MM/yyyyHH:mm", Locale.getDefault()).parse(binding.datePickerBTN.text.toString() + binding.timePickerBTN.text.toString())
            } else {
                null
            }
            val task = Task(
                binding.taskName.text.toString(),
                binding.taskDescription.text.toString(),
                dueDateTime?.time ?: 0L,
                System.currentTimeMillis(),
                ArrayList(usedTags.map { it.key }),
                binding.checkbox.isChecked,
                if (currTask != null) currTask!!.key else ""
            )
            viewModel.addTask(task).addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "${task.name} added/edited",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            finish()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
        if (pos != 0) {
            usedTags.add(unusedTags[pos])
            unusedTags.remove(unusedTags[pos])
            binding.recyclerView.adapter =
                TagsRecyclerAdapter(usedTags, unusedTags, binding.tagsSpinner)
            if (unusedTags.size == 1) {
                binding.tagsSpinner.visibility = View.GONE
            }
        }
        binding.tagsSpinner.setSelection(0)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

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
                if (currTask != null) {

                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setCancelable(true)
                    builder.setTitle("Are you sure you want to delete this?")
                    builder.setMessage("Deleted data cannot be recovered")
                    builder.setPositiveButton("Delete") { dialog, which ->
                        deleteCurrTask()
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

    private fun deleteCurrTask() {
        if (currTask != null) {
            viewModel.deleteTask(currTask!!).addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "${currTask!!.name} deleted from tasks",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        finish()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        var time = ""
        time += if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }
        time += ":"
        time += if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
        return time
    }

    override fun finish() {
        viewModel.setCurrTask(null)
        super.finish()
    }
}