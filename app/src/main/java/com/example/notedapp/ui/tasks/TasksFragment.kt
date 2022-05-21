package com.example.notedapp.ui.tasks

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.barisatalay.filterdialog.FilterDialog
import com.barisatalay.filterdialog.model.DialogListener
import com.barisatalay.filterdialog.model.DialogListener.Multiple
import com.example.notedapp.FabButtonClick
import com.example.notedapp.MainActivity
import com.example.notedapp.MainViewModel
import com.example.notedapp.R
import com.example.notedapp.databinding.FragmentTasksBinding
import com.example.notedapp.models.Tag
import com.example.notedapp.models.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


@SuppressLint("NotifyDataSetChanged")
class TasksFragment : Fragment(), FabButtonClick {

    private val TAG = "TasksFragment"

    private lateinit var binding: FragmentTasksBinding
    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var database: DatabaseReference
    private val tags = ArrayList<Tag>()
    private val allTasks = ArrayList<Task>()
    private val filteredAndSortedTasks = ArrayList<Task>()

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        database = Firebase.database.reference.child("users").child(viewModel.getCurrUserEmail())
            .child("tasks")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTasksBinding.inflate(inflater, container, false)
        val root: View = binding.root
        (activity as MainActivity).setListener(this)

        binding.recyclerView.adapter = TasksRecyclerAdapter(filteredAndSortedTasks, viewModel)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 1)

        binding.empty.visibility = View.INVISIBLE
        binding.loading.visibility = View.VISIBLE

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allTasks.clear()
                for (snapshot in dataSnapshot.children) {
                    val task = snapshot.getValue(Task::class.java)!!
                    allTasks.add(task)
                }

                filteredAndSortedTasks.clear()
                filteredAndSortedTasks.addAll(allTasks)
                filterTasks(null)
                sortTasks(null)

                binding.loading.visibility = View.INVISIBLE
                if (allTasks.isEmpty()) {
                    binding.empty.visibility = View.VISIBLE
                } else {
                    binding.empty.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        Firebase.database.reference.child("users").child(viewModel.getCurrUserEmail()).child("tags")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    tags.clear()
                    for (snapshot in dataSnapshot.children) {
                        tags.add(snapshot.getValue(Tag::class.java)!!)
                    }
                    filterTasks(null)
                    sortTasks(null)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                val filterDialog = FilterDialog(activity)

                filterDialog.toolbarTitle = "Filter Tasks By:"
                filterDialog.searchBoxHint = "Search"
                filterDialog.setSelectButtonText("Filter")
                val filters = arrayListOf(
                    Tag("Completed", "#50C878", -1, "done"),
                    Tag("Incomplete", "#50C878", -1, "notDone")
                )
                filters.addAll(tags)
                filterDialog.setList(filters)
                filterDialog.selectableCount = filters.size
                filterDialog.setOnCloseListener {
                    filterDialog.dispose()
                }
                filterDialog.show("key", "name", Multiple { selectedItems ->
                    filterTasks(selectedItems.map { it.code })
                    Toast.makeText(context, "Tasks filtered", Toast.LENGTH_SHORT).show()
                    filterDialog.dispose()
                })
                return true
            }
            R.id.action_sort -> {
                val filterDialog = FilterDialog(activity)

                filterDialog.toolbarTitle = "Sort Tasks By:"
                filterDialog.searchBoxHint = "Search"
                filterDialog.setSelectButtonText("Sort")
                val sorts = arrayListOf(
                    "Date created (ascending)",
                    "Date created (descending)",
                    "Date due (ascending)",
                    "Date due (descending)",
                )
                filterDialog.setList(sorts)
                filterDialog.selectableCount = 1
                filterDialog.show(DialogListener.Single { selectedItem ->
                    sortTasks(selectedItem.name)
                    Toast.makeText(context, "Tasks sorted", Toast.LENGTH_SHORT).show()
                    filterDialog.dispose()
                })
                return true
            }
            else -> {}
        }

        return false
    }

    private fun filterTasks(filterKeys: List<String>?) {
        filteredAndSortedTasks.clear()

        var newFilterKeys = listOf<String>()

        if (activity == null) {
            if (filterKeys != null) {
                newFilterKeys = filterKeys
            }
        } else {
            val sp = activity!!.getSharedPreferences("tasksSettings", MODE_PRIVATE)
            newFilterKeys = if (filterKeys == null) {
                sp!!.getStringSet("filter", setOf<String>())!!.toList()
            } else {
                val spEdit = sp.edit()
                spEdit.putStringSet("filter", filterKeys.toSet())
                spEdit.apply()
                filterKeys
            }
        }

        var filtersString = "Filters: "
        for (filterKey in newFilterKeys) {

            if (filterKey == "done") {
                filtersString += if (filtersString == "Filters: ") {
                    "Completed"
                } else {
                    ", Complete"
                }
                continue
            } else if (filterKey == "notDone") {
                filtersString += if (filtersString == "Filters: ") {
                    "Incomplete"
                } else {
                    ", Incomplete"
                }
                continue
            }

            for (tag in tags) {
                if (tag.key == filterKey) {
                    filtersString += if (filtersString == "Filters: ") {
                        tag.name
                    } else {
                        ", ${tag.name}"
                    }
                    break
                }
            }
        }

        if (newFilterKeys.isEmpty()) {
            filtersString = "Filters: None"
            filteredAndSortedTasks.addAll(allTasks)
        } else {
            for (task in allTasks) {
                var hasAllTags = true
                for (key in newFilterKeys) {
                    if (
                        (key == "done" && task.done) ||
                        (key == "notDone" && !task.done) ||
                        (key in task.tagKeys)
                    ) {
                        // task has this filter tag
                    } else {
                        // task does not have this filter tag
                        hasAllTags = false
                        break
                    }
                }
                if (hasAllTags) {
                    filteredAndSortedTasks.add(task)
                }
            }
        }
        sortTasks(null)
        binding.filters.text = filtersString
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun sortTasks(sorting: String?) {

        var newSorting = "Date due (ascending)"

        if (activity != null) {
            val sp = activity!!.getSharedPreferences("tasksSettings", MODE_PRIVATE)
            newSorting = if (sorting == null) {
                sp!!.getString("sort", "Date due (ascending)")!!
            } else {
                val spEdit = sp.edit()
                spEdit.putString("sort", sorting)
                spEdit.apply()
                sorting
            }
        }

        when (newSorting) {
            "Date created (ascending)" ->
                filteredAndSortedTasks.sortBy { it.timeCreated }
            "Date created (descending)" ->
                filteredAndSortedTasks.sortByDescending { it.timeCreated }
            "Date due (ascending)" ->
                filteredAndSortedTasks.sortBy { it.timeDue }
            "Date due (descending)" ->
                filteredAndSortedTasks.sortByDescending { it.timeDue }
        }

        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onFabClicked() {
        startActivity(Intent(context, EditTaskActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        viewModel.setCurrTask(null)
    }
}