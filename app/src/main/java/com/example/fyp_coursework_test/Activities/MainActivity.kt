package com.example.fyp_coursework_test.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_coursework_test.Adapters.FragmentsAdapter
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Link the Activity to the XML Layout
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialise Firebase Tools
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")


            buttonOnClickListeners()
            setTabLayout()
            personalise()

        }
    }

    // Set all the onClickListeners for the buttons
    private fun buttonOnClickListeners() {

        // Navigate to settings when click on profile image
        binding.toolbar.imageIcon.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    // Displays the current user's profile image in the correct image view and shows admin button if
    // user is the master admin
    private fun personalise() {
        auth.currentUser?.let { user ->
            val uidReference = database.reference.child("Users").child(user.uid)

            uidReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profilePicUrl = snapshot.child("profile_Pic").getValue(String::class.java)

                    Picasso.get()
                        .load(profilePicUrl)
                        .placeholder(R.drawable.ic_baseline_account_circle_24_offwhite)
                        .into(binding.toolbar.imageIcon)

                    // Display admin button if user is master admin
                    val isMasterAdmin = snapshot.child("master_Admin").getValue(Boolean::class.java) ?: false
                    if (isMasterAdmin) {
                        binding.toolbar.adminPageButton.visibility = View.VISIBLE
                        binding.toolbar.adminPageButton.setOnClickListener{
                            val intent = Intent(this@MainActivity, AdminActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        binding.toolbar.adminPageButton.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            // User is not authenticated, navigate to sign in activity
            val intent = Intent(this@MainActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // set custom fragment adapter to tab layout
    private fun setTabLayout() {
        binding.viewPager.adapter = FragmentsAdapter(supportFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_baseline_home_24)
        binding.tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_baseline_groups_24)
        binding.tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_baseline_chat_bubble_24)
        binding.tabLayout.getTabAt(3)!!.setIcon(R.drawable.ic_baseline_event_24)
    }

}