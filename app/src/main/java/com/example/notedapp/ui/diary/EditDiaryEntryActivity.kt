package com.example.notedapp.ui.diary

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import androidx.appcompat.content.res.AppCompatResources
import com.example.notedapp.MainViewModel
import com.example.notedapp.R
import com.example.notedapp.databinding.ActivityEditDiaryEntryBinding
import com.example.notedapp.models.DiaryEntry
import com.example.notedapp.models.Mood
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
import android.content.DialogInterface





class EditDiaryEntryActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityEditDiaryEntryBinding
    private val viewModel by viewModels<MainViewModel>()
    private val TAG = "EditDiaryEntryActivityTag"
    private lateinit var database: DatabaseReference
    private val allTasks = ArrayList<Task>()


    private val usedTasks: ArrayList<Task> = ArrayList()
    private val unusedTasks: ArrayList<Task> = ArrayList()
    private var currEntry: DiaryEntry? = null
    private var mood = Mood.CONTENT

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDiaryEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        database = Firebase.database.reference.child("users").child(viewModel.getCurrUserEmail())
            .child("tasks")

        if (unusedTasks.isEmpty()) {
            unusedTasks.add(Task("Loading tasks..."))
        }

        currEntry = viewModel.getCurrEntry()

        binding.moodsSpinner.adapter = MoodsSpinnerAdapter(applicationContext, Mood.values())
        binding.datePickerBTN.text = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(Date(System.currentTimeMillis()))

        if (currEntry != null) {
            binding.entryTitle.setText(currEntry!!.title)
            binding.entryDescription.setText(currEntry!!.description)
            binding.moodsSpinner.setSelection(Mood.values().indexOf(currEntry!!.mood))
            binding.datePickerBTN.text = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date(currEntry!!.date))
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allTasks.clear()
                for (snapshot in dataSnapshot.children) {
                    allTasks.add(snapshot.getValue(Task::class.java)!!)
                }
                unusedTasks.clear()
                unusedTasks.add(Task("Add task"))

                usedTasks.clear()

                if (currEntry != null) {

                    for (task in allTasks) {
                        for (taskKey in currEntry!!.taskKeys) {
                            if (task.key == taskKey) {
                                usedTasks.add(task)
                            }
                        }
                    }

                    for (task in allTasks) {
                        var duplicate = false
                        for (usedTask in usedTasks) {
                            if (usedTask.key == task.key) {
                                duplicate = true
                                break
                            }
                        }
                        if (!duplicate) {
                            unusedTasks.add(task)
                        }
                    }
                } else {
                    unusedTasks.addAll(allTasks)
                }
                if (unusedTasks.size == 1) {
                    binding.tasksSpinner.visibility = View.GONE
                }
                binding.tasksSpinner.setSelection(0)
                binding.tasksSpinner.adapter = TasksSpinnerAdapter(applicationContext, unusedTasks)
                binding.recyclerView.adapter =
                    TasksRecyclerAdapter(usedTasks, unusedTasks, binding.tasksSpinner)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        binding.tasksSpinner.onItemSelectedListener = this
        binding.moodsSpinner.onItemSelectedListener = this
        binding.recyclerView.layoutManager = FlexboxLayoutManager(applicationContext)

        binding.datePickerBTN.setOnClickListener {
            val dialogDate = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, Y, M, D ->
                // M is 0 to 11 (0 indexed)
                binding.datePickerBTN.text = "$D/${M + 1}/$Y"
                Log.i(TAG, binding.datePickerBTN.text.toString())
            }
            val year = dialogDate.get(Calendar.YEAR)
            val month = dialogDate.get(Calendar.MONTH)
            val day = dialogDate.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, dateSetListener, year, month, day).show()
        }

        binding.fab.setOnClickListener {

            if (binding.entryTitle.text.isBlank()) {
                Toast.makeText(applicationContext, "Please add an entry name", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val entry = DiaryEntry(
                binding.entryTitle.text.toString(),
                binding.entryDescription.text.toString(),
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).parse(binding.datePickerBTN.text.toString())!!.time,
                ArrayList(usedTasks.map { it.key }),
                mood
            )
            if (currEntry != null){
                viewModel.deleteDiaryEntry(currEntry!!)
            }
            viewModel.addDiaryEntry(entry).addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "${entry.title} added/edited",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            finish()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {

        when (p0!!.id) {
            R.id.tasks_spinner -> {
                if (pos != 0) {
                    usedTasks.add(unusedTasks[pos])
                    unusedTasks.remove(unusedTasks[pos])
                    binding.recyclerView.adapter =
                        TasksRecyclerAdapter(usedTasks, unusedTasks, binding.tasksSpinner)
                    if (unusedTasks.size == 1) {
                        binding.tasksSpinner.visibility = View.GONE
                    }
                }
                binding.tasksSpinner.setSelection(0)
            }

            R.id.moods_spinner -> {
                mood = Mood.values()[pos]
            }
        }
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
                if (currEntry != null) {

                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setCancelable(true)
                    builder.setTitle("Are you sure you want to delete this?")
                    builder.setMessage("Deleted data cannot be recovered")
                    builder.setPositiveButton("Delete") { dialog, which ->
                        deleteCurrEntry()
                    }
                    builder.setNegativeButton(android.R.string.cancel) { dialog, which ->

                    }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()

                } else {
                    Toast.makeText(
                        applicationContext,
                        "You need to add this entry before deleting it",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
            else -> {}
        }

        return false
    }

    private fun deleteCurrEntry() {
        if (currEntry != null) {
            viewModel.deleteDiaryEntry(currEntry!!).addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "${currEntry!!.title} deleted from diary",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        finish()
    }

    override fun finish() {
        viewModel.setCurrTask(null)
        super.finish()
    }
}