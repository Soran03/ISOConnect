package com.example.fyp_coursework_test.Activities

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_coursework_test.Adapters.JoinIsocsAdapter
import com.example.fyp_coursework_test.Models.IsocModel
import com.example.fyp_coursework_test.databinding.ActivityJoinIsocBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JoinIsocActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJoinIsocBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var isocsAdapter : JoinIsocsAdapter
    private var isocList = ArrayList<IsocModel>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            binding = ActivityJoinIsocBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

            isocsAdapter = JoinIsocsAdapter(this, isocList, database)
            val layoutManager = LinearLayoutManager(this)
            binding.joinIsocRecyclerView.layoutManager = layoutManager
            binding.joinIsocRecyclerView.adapter = isocsAdapter
            fetchIsocs()
        }
    }

    private fun fetchIsocs() {
        val reference = database.reference.child("Isocs")
        val userId = auth.currentUser?.uid ?: return

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    isocList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val isoc = snapshot.getValue(IsocModel::class.java)
                        if (isoc != null && userId !in isoc.memberList) {
                            isocList.add(isoc)
                        }
                    }
                    // Notify adapter about the data change
                    isocsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("JoinIsocActivity", "Error fetching Isocs: ${error.message}")
                }
            })
    }
}
