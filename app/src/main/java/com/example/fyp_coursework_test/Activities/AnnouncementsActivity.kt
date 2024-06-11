package com.example.fyp_coursework_test.Activities

import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_coursework_test.Adapters.AnnouncementAdapter
import com.example.fyp_coursework_test.Models.AnnouncementModel
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.ActivityAnnouncementsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Date
import java.util.Objects

class AnnouncementsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAnnouncementsBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase

    private val announcementsList = ArrayList<AnnouncementModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Link the Activity to the XML Layout
            binding = ActivityAnnouncementsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialise Firebase Tools
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

            buttonOnClickListeners()
            setToolbar()
            showAnnouncements()
            showCreateAnnouncementsLayout()


        }
    }

    // Set all the onClickListeners for the buttons
    private fun buttonOnClickListeners() {

        // return to main activity
        binding.announcementsBackButton.setOnClickListener{
            finish()
        }
    }

    // fill in the toolbar with correct isoc image and name
    private fun setToolbar() {
        val isocName = intent.getStringExtra("isocName")
        val isocProfilePic = intent.getStringExtra("profilePic")

        binding.announcementsUsername.text = isocName
        Picasso.get().load(isocProfilePic)
            .placeholder(R.drawable.ic_baseline_account_circle_24_offwhite)
            .into(binding.announcementsProfileImage)
    }


    private fun showAnnouncements() {
        // set the announcement recycler view adapter
        val announcementAdapter = AnnouncementAdapter(this, announcementsList)
        val layoutManager = LinearLayoutManager(this)
        binding.announcementsRecyclerView.layoutManager = layoutManager
        binding.announcementsRecyclerView.adapter = announcementAdapter

        // check the database for all announcements in the chat room and load them up
        database.reference
            .child("Announcements")
            .child(intent.getStringExtra("isocId").toString())
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        // Fill newAnnouncements list with all the announcements
                        val newAnnouncements = ArrayList<AnnouncementModel>()
                        for (snapshot: DataSnapshot in dataSnapshot.children) {
                            val announcementModel = snapshot.getValue(AnnouncementModel::class.java)
                            announcementModel?.let {
                                newAnnouncements.add(it)
                            }
                        }

                        // Update announcement list on the UI thread
                        runOnUiThread {
                            announcementsList.clear()
                            announcementsList.addAll(newAnnouncements)
                            announcementAdapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    // add announcement to the database
    private fun createAnnouncement() {
        val title = binding.announcementTitleInputEditText.text.toString().trim()
        val description = binding.announcementDescriptionInputEditText.text.toString().trim()
        val timestamp = Date().time

        val announcement = AnnouncementModel(
            announcementTitle = title,
            description = description,
            timestamp = timestamp
        )

        // if title and desc valid, add to database
        if (title.isNotEmpty() && description.isNotEmpty()) {
            database.reference
                .child("Announcements")
                .child(intent.getStringExtra("isocId").toString())
                .push()
                .setValue(announcement)
        }
        else {
            Toast.makeText(this@AnnouncementsActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()

        }
    }

    // only allow admin to create announcements
    private fun showCreateAnnouncementsLayout() {
        database.reference
            .child("Users")
            .child(auth.currentUser!!.uid)
            .child("master_Admin")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isMasterAdmin = snapshot.getValue(Boolean::class.java) ?: false

                    // if user is an admin, show the add announcement features
                    if (isMasterAdmin) {
                        binding.announcementCreateViewMoreButton.visibility = View.VISIBLE
                        binding.addAnnouncementButton.setOnClickListener{
                            createAnnouncement()
                        }
                        collapseCreateAnn()
                    }
                    else {
                        binding.announcementCreateViewMoreButton.visibility = View.GONE
                        binding.createAnnouncementLayout.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun expandCreateAnn() {
        TransitionManager.beginDelayedTransition(binding.createAnnouncementLayout as ViewGroup) // Animate the transition
        binding.createAnnouncementLayout.visibility = View.VISIBLE
        binding.announcementCreateViewMoreButton.setOnClickListener{
            collapseCreateAnn()
        }
        binding.announcementCreateViewMoreButton.icon = ContextCompat.getDrawable(this, R.drawable.baseline_expand_more_24)
    }

    private fun collapseCreateAnn() {
        TransitionManager.beginDelayedTransition(binding.createAnnouncementLayout as ViewGroup) // Animate the transition
        binding.createAnnouncementLayout.visibility = View.GONE
        binding.announcementCreateViewMoreButton.setOnClickListener{
            expandCreateAnn()
        }
        binding.announcementCreateViewMoreButton.icon = ContextCompat.getDrawable(this, R.drawable.baseline_expand_less_24)
    }
}
