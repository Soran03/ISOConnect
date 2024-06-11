package com.example.fyp_coursework_test.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_coursework_test.Adapters.UsersAdapter
import com.example.fyp_coursework_test.Models.IsocModel
import com.example.fyp_coursework_test.Models.UserModel
import com.example.fyp_coursework_test.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdminBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase

    private lateinit var usersAdapter : UsersAdapter
    private var usersList = ArrayList<UserModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            binding = ActivityAdminBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

            usersAdapter = UsersAdapter(this, usersList, 2)
            val layoutManager = LinearLayoutManager(this)
            binding.allUsersRecyclerView.layoutManager = layoutManager
            binding.allUsersRecyclerView.adapter = usersAdapter
            showUsers()

            binding.adminCreateIsocButton.setOnClickListener{
                createIsoc()
            }
        }
    }


    private fun createIsoc() {
        val isocName = binding.adminIsocNameInputEditText.text.toString().trim()
        val isocId = database.reference.child("Isocs").push().key ?: return

        val isoc = IsocModel(
            isocId = isocId,
            isocName = isocName)

        database.reference.child("Isocs").child(isocId).setValue(isoc).
                addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@AdminActivity,
                            "ISOC Group created Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@AdminActivity,
                            "Creation failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }

    private fun showUsers() {

        val reference = database.reference.child("Users")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersList.clear()
                for (snapshot: DataSnapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(UserModel::class.java)
                    if (user != null && user.user_ID != auth.currentUser!!.uid) {
                        val userId = user.user_ID
                        val key = snapshot.key

                        usersList.add(user)
                    }
                }

                usersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

