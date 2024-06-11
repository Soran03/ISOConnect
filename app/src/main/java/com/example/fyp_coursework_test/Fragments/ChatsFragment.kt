package com.example.fyp_coursework_test.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_coursework_test.Adapters.IsocsAdapter
import com.example.fyp_coursework_test.Adapters.UsersAdapter
import com.example.fyp_coursework_test.Models.UserModel
import com.example.fyp_coursework_test.databinding.FragmentChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChatsFragment : Fragment() {
    private lateinit var binding : FragmentChatsBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase

    private lateinit var usersAdapter : UsersAdapter
    private var usersList = ArrayList<UserModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Link the Activity to the XML Layout
        binding = FragmentChatsBinding.inflate(layoutInflater)

        // Initialise Firebase Tools
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

        setRecyclerView()
        fetchUsers()

        return binding.root
    }

    // Set the correct adapter to the recycler view
    private fun setRecyclerView () {
        usersAdapter = UsersAdapter(requireContext(), usersList, 1)
        val layoutManager = LinearLayoutManager(context)
        binding.chatsRecyclerView.layoutManager = layoutManager
        binding.chatsRecyclerView.adapter = usersAdapter
    }

    // greensList is updated from Firebase, notifying the UsersAdpater to display all users
    private fun fetchUsers() {
        val reference = database.reference.child("Users")

        // Check all users in database
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersList.clear()
                for (snapshot: DataSnapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(UserModel::class.java)

                    // If user is valid, add to the list
                    if (user != null && user.user_ID != auth.currentUser?.uid) {
                        usersList.add(user)
                    }
                }
                // notify the adapter
                usersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}