package com.example.fyp_coursework_test.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_coursework_test.Adapters.IsocsAdapter
import com.example.fyp_coursework_test.Activities.JoinIsocActivity
import com.example.fyp_coursework_test.Models.IsocModel
import com.example.fyp_coursework_test.databinding.FragmentIsocBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class IsocFragment : Fragment() {
    private lateinit var binding : FragmentIsocBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase

    private lateinit var isocsAdapter : IsocsAdapter
    private var isocList = ArrayList<IsocModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Link the Activity to the XML Layout
        binding = FragmentIsocBinding.inflate(layoutInflater)

        // Initialise Firebase Tools
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")


        setRecyclerView()
        showIsocs()
        buttonOnClickListeners()

        return binding.root
    }

    // Set all the onClickListeners for the buttons
    private fun buttonOnClickListeners() {

        // Navigate to Join Isoc Activity
        binding.isocsJoinButton.setOnClickListener{
            val intent = Intent(binding.isocsJoinButton.context, JoinIsocActivity::class.java)
            startActivity(intent)
        }
    }

    // Set the correct adapter to the recycler view
    private fun setRecyclerView () {
        isocsAdapter = IsocsAdapter(requireContext(), isocList)
        val layoutManager = LinearLayoutManager(context)
        binding.isocsRecyclerView.layoutManager = layoutManager
        binding.isocsRecyclerView.adapter = isocsAdapter
    }


    // IsocList is updated from Firebase, notifying the isocsAdpater to display
    // correct number of ISOC groups
    private fun showIsocs() {
        val reference = database.reference.child("Isocs")
        val userId = auth.currentUser!!.uid

        // Check all ISOC groups in database
        reference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                isocList.clear()
                for (snapshot: DataSnapshot in dataSnapshot.children) {
                    val isoc = snapshot.getValue(IsocModel::class.java)

                    // If user is part of ISOC member list, add to the list
                    if (isoc != null && userId in isoc.memberList) {
                        Log.d("isocModel", isoc.toString())
                        isocList.add(isoc)
                    }
                }
                // notify the adapter
                isocsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}