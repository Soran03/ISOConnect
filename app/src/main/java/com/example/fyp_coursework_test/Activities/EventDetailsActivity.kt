package com.example.fyp_coursework_test.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_coursework_test.Adapters.ChatAdapter
import com.example.fyp_coursework_test.Adapters.EventCommentsAdapter
import com.example.fyp_coursework_test.Models.CommentModel
import com.example.fyp_coursework_test.Models.EventModel
import com.example.fyp_coursework_test.Models.MessageModel
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.ActivityEventDetailsBinding
import com.example.fyp_coursework_test.databinding.ActivityMainBinding
import com.google.android.datatransport.runtime.firebase.transport.LogEventDropped
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventDetailsActivity: AppCompatActivity() {
    private lateinit var binding : ActivityEventDetailsBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase

    private lateinit var eventId : String

    private val commentList = ArrayList<CommentModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Link the Activity to the XML Layout
            binding = ActivityEventDetailsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialise Firebase Tools
            auth = FirebaseAuth.getInstance()
            database =
                FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

            eventId = intent.getStringExtra("eventId").toString()
            Log.d("EventDetailsSnapshot", eventId.toString())


            loadEventDetails()
            setHeartIcon()
            showEventComments()
            setOnClickListeners()

        }
    }

    private fun setOnClickListeners() {
        binding.eventDetailsBackButton.setOnClickListener {
            finish()
        }

        binding.chatDetailsSendCommentButton.setOnClickListener {
            sendCommentToDatabase()
        }

        binding.eventDetailsLikeButton.setOnClickListener {
            likeEvent()
        }
    }

    private fun loadEventDetails() {

        val eventRef = database.reference.child("Events").child(eventId)
        eventRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                binding.eventDetailsTitle.text =
                    snapshot.child("eventName").getValue(String::class.java)
                binding.eventDetailsAddressText.text =
                    snapshot.child("address").getValue(String::class.java)
                binding.eventDetailsDateText.text =
                    formatEventDate(snapshot.child("eventDate").getValue(Date::class.java))
                binding.eventDetailsDescriptionText.text =
                    snapshot.child("description").getValue(String::class.java)
                binding.eventDetailsLikeCountText.text =
                    snapshot.child("likedList").childrenCount.toInt().toString()


                val eventImage = snapshot.child("imageUrl").getValue(String::class.java)
                binding.eventDetailsImage.setBackgroundResource(android.R.color.transparent)
                Picasso.get()
                    .load(eventImage)
                    .placeholder(R.drawable.baseline_image_24)
                    .into(binding.eventDetailsImage)

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun showEventComments() {
        val commentsAdapter = EventCommentsAdapter(this, commentList)
        val layoutManager = LinearLayoutManager(this)
        binding.eventDetailsCommentsRecyclerView.layoutManager = layoutManager
        binding.eventDetailsCommentsRecyclerView.adapter = commentsAdapter

        database.reference
            .child("Comments")
            .child(eventId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        val newComments = ArrayList<CommentModel>()
                        for (snapshot: DataSnapshot in dataSnapshot.children) {
                            val commentModel = snapshot.getValue(CommentModel::class.java)
                            commentModel?.let {
                                newComments.add(it)
                            }
                        }

                        // Update message list on the UI thread
                        runOnUiThread {
                            commentList.clear()
                            commentList.addAll(newComments)
                            commentsAdapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }

    private fun sendCommentToDatabase() {
        val commentInput = binding.eventDetailsCommentInput.text.toString().trim()
        val timestamp = Date().time

        val commentModel = CommentModel(auth.currentUser!!.uid, commentInput, timestamp)

        if (commentInput.isNotEmpty()) {
            database.reference
                .child("Comments")
                .child(eventId)
                .push()
                .setValue(commentModel)
        }
    }

    private fun formatEventDate(date: Date?): String {
        // Check if the date is null
        if (date == null) {
            return ""
        }

        // Define the desired date format
        val dateFormat = SimpleDateFormat("EEEE d'st' MMMM yyyy", Locale.getDefault())

        // Customize the day suffix (st, nd, rd, th)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val suffix = when {
            dayOfMonth in 11..13 -> "th"
            dayOfMonth % 10 == 1 -> "st"
            dayOfMonth % 10 == 2 -> "nd"
            dayOfMonth % 10 == 3 -> "rd"
            else -> "th"
        }

        // Replace the day suffix placeholder
        return dateFormat.format(date).replace("st", suffix)
    }


    private fun likeEvent() {
        val userId = auth.currentUser!!.uid

        val likedListRef = database.reference
            .child("Events")
            .child(eventId)

        likedListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val event = dataSnapshot.getValue(EventModel::class.java)
                if (event != null) {
                    if (!event.likedList.contains(userId)) {
                        val updatedLiked = event.likedList.toMutableList()
                        updatedLiked.add(userId)
                        likedListRef.child("likedList").setValue(updatedLiked)
                        binding.eventDetailsLikeButton.setIconResource(R.drawable.icon_heart)

                    }
                    else {
                        val updatedLiked = event.likedList.toMutableList()
                        updatedLiked.remove(userId)
                        likedListRef.child("likedList").setValue(updatedLiked)
                        binding.eventDetailsLikeButton.setIconResource(R.drawable.icon_heart_outline)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("likeEvent", "Database error", error.toException())
            }
        })
    }

    private fun setHeartIcon() {
        val userId = auth.currentUser!!.uid

        val likedListRef = database.reference
            .child("Events")
            .child(eventId)
            .child("likedList")

        likedListRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(userId)) binding.eventDetailsLikeButton.setIconResource(R.drawable.icon_heart)
                else binding.eventDetailsLikeButton.setIconResource(R.drawable.icon_heart_outline)

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}