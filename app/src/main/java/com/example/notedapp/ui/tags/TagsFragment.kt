package com.example.notedapp.ui.tags

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.notedapp.FabButtonClick
import com.example.notedapp.MainActivity
import com.example.notedapp.MainViewModel
import com.example.notedapp.R
import com.example.notedapp.databinding.FragmentTagsBinding
import com.example.notedapp.models.Tag
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TagsFragment : Fragment(), FabButtonClick {

    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var binding: FragmentTagsBinding
    private lateinit var database: DatabaseReference
    private val tags = ArrayList<Tag>()
    private val TAG = "TagsFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        database = Firebase.database.reference.child("users").child(viewModel.getCurrUserEmail())
            .child("tags")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTagsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        (activity as MainActivity).setListener(this)

        binding.recyclerView.layoutManager = GridLayoutManager(context, 1)
        binding.recyclerView.adapter = TagsRecyclerAdapter(tags, viewModel)
        binding.empty.visibility = View.INVISIBLE
        binding.loading.visibility = View.VISIBLE

        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tags.clear()
                for (snapshot in dataSnapshot.children) {
                    tags.add(snapshot.getValue(Tag::class.java)!!)
                }
                tags.sortByDescending { it.numTasks }
                binding.recyclerView.adapter?.notifyDataSetChanged()
                binding.loading.visibility = View.INVISIBLE
                if (tags.isEmpty()) {
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
        startActivity(Intent(context, EditTagActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        viewModel.setCurrTag(null)
    }
}