package com.example.fyp_coursework_test.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_coursework_test.Models.CommentModel
import com.example.fyp_coursework_test.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventCommentsAdapter(private val context: Context, private val commentsList: ArrayList<CommentModel>) :
    RecyclerView.Adapter<EventCommentsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventCommentUserImage: ImageView = itemView.findViewById(R.id.eventCommentUserImage)
        val eventCommentUsername: TextView = itemView.findViewById(R.id.eventCommentUsername)
        val eventCommentMessage: TextView = itemView.findViewById(R.id.eventCommentText)
        val eventCommentTime: TextView = itemView.findViewById(R.id.eventCommentTime)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventCommentsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_event_comment, parent, false)
        return EventCommentsAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentsList[position]

        holder.eventCommentMessage.text = comment.comment
        holder.eventCommentTime.text = formatTimestamp(comment.timestamp)

        FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
            .child("Users")
            .child(comment.userId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    val profilePicUrl = snapshot.child("profile_Pic").getValue(String::class.java)

                    holder.eventCommentUsername.text = username.toString()
                    Picasso.get().load(profilePicUrl)
                        .placeholder(R.drawable.profile_circle_svgrepo_com)
                        .into(holder.eventCommentUserImage)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    private fun formatTimestamp(timestamp: Long): String {
        val now = Calendar.getInstance()
        val commentTime = Calendar.getInstance()
        commentTime.timeInMillis = timestamp

        // If the message was sent today
        if (now.get(Calendar.YEAR) == commentTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == commentTime.get(Calendar.DAY_OF_YEAR)) {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return dateFormat.format(Date(timestamp))
        }

        // If the message was sent yesterday
        now.add(Calendar.DAY_OF_YEAR, -1)
        if (now.get(Calendar.YEAR) == commentTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == commentTime.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday"
        }

        // dates before yesterday
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

}