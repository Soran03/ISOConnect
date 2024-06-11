package com.example.fyp_coursework_test.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var messaging: FirebaseMessaging


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Link the Activity to the XML Layout
            binding = ActivitySettingsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialise Firebase Tools
            auth = FirebaseAuth.getInstance()
            database =
                FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

            buttonOnClickListeners()
            personalise()
        }
    }

    // Set all the onClickListeners for the buttons
    private fun buttonOnClickListeners() {
        binding.signOutButton.setOnClickListener{signOut()}
        binding.settingsBackButton.setOnClickListener{finish()}
    }

    // Signs out user from Authentication and go back to Sign In Activity
    private fun signOut(){
        auth.signOut()
        val intent = Intent(this@SettingsActivity, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Displays the current user's profile image, username and full name in the correct image and text views
    private fun personalise() {
        auth.currentUser!!.let { user ->
            val uidReference = database.reference.child("Users").child(user.uid)

            uidReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profilePicUrl = snapshot.child("profile_Pic").getValue(String::class.java)
                    val username = snapshot.child("username").getValue(String::class.java)
                    val firstname = snapshot.child("firstname").getValue(String::class.java)
                    val lastname = snapshot.child("lastname").getValue(String::class.java)
                    val fullname = "$firstname $lastname"


                    Picasso.get()
                        .load(profilePicUrl)
                        .placeholder(R.drawable.ic_baseline_account_circle_24_offwhite)
                        .into(binding.settingsProfileImage)

                    binding.settingsUsernameText.text = username
                    binding.settingsFullnameText.text = fullname
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }
}
