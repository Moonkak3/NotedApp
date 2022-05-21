package com.example.notedapp.ui.diary

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
import com.example.notedapp.databinding.FragmentDiaryBinding
import com.example.notedapp.models.DiaryEntry
import com.example.notedapp.models.Tag
import com.example.notedapp.models.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


@SuppressLint("NotifyDataSetChanged")
class DiaryFragment : Fragment(), FabButtonClick {

    private val TAG = "DiaryFragment"

    private lateinit var binding: FragmentDiaryBinding
    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var database: DatabaseReference
    private val allEntries = ArrayList<DiaryEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        database = Firebase.database.reference.child("users").child(viewModel.getCurrUserEmail())
            .child("diaryEntries")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDiaryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        (activity as MainActivity).setListener(this)

        binding.recyclerView.adapter = DiaryRecyclerAdapter(allEntries, viewModel)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 1)

        binding.empty.visibility = View.INVISIBLE
        binding.loading.visibility = View.VISIBLE

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allEntries.clear()
                for (snapshot in dataSnapshot.children) {
                    allEntries.add(snapshot.getValue(DiaryEntry::class.java)!!)
                }
                allEntries.sortByDescending { it.date }
                binding.recyclerView.adapter?.notifyDataSetChanged()

                binding.loading.visibility = View.INVISIBLE
                if (allEntries.isEmpty()) {
                    binding.empty.visibility = View.VISIBLE
                } else {
                    binding.empty.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        return root
    }

    override fun onFabClicked() {
        startActivity(Intent(context, EditDiaryEntryActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        viewModel.setCurrEntry(null)
    }
}