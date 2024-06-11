package com.example.fyp_coursework_test.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_coursework_test.Models.MessageModel
import com.example.fyp_coursework_test.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.log

class ChatAdapter(private val context: Context,
                  private val chatType: Int,
                  private val messageList: ArrayList<MessageModel>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_RECEIVER = 1
        const val VIEW_TYPE_SENDER = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        // different layout for if its a sent or received message
        return when (viewType) {
            VIEW_TYPE_SENDER -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.sample_sender_msg, parent, false)
                SenderViewHolder(view)
            }
            VIEW_TYPE_RECEIVER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.sample_receiver_msg, parent, false)
                ReceiverViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // bind all message data to the layout
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messageModel = messageList[position]

        when (holder) {

            // sent message binding
            is SenderViewHolder -> {
                holder.senderMessage.text = messageModel.message
                holder.senderTime.text = formatDate(messageModel.timestamp, TimeZone.getDefault())
            }

            // received message binding
            is ReceiverViewHolder -> {
                holder.receiverMessage.text = messageModel.message
                holder.receiverTime.text = formatDate(messageModel.timestamp, TimeZone.getDefault())

                // show received message user's profile image and username from database if it is a group chat
                if (isMultiChat()) {
                    holder.receiverImage.visibility = View.VISIBLE
                    holder.receiverName.visibility = View.VISIBLE
                    Log.d("usernameProfile", messageModel.user_ID)

                    FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")
                        .reference
                        .child("Users")
                        .child(messageModel.user_ID)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val username = snapshot.child("username").getValue(String::class.java)
                                Log.d("usernameProfile", username.toString())

                                holder.receiverName.text = username.toString()

                                val profilePicUrl = snapshot.child("profile_Pic").getValue(String::class.java)
                                Picasso.get().load(profilePicUrl)
                                    .placeholder(R.drawable.profile_circle_svgrepo_com)
                                    .into(holder.receiverImage)

                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("usernameProfile", "nope")
                            }
                        })
                    Log.d("usernameProfile", "AFTER")



                } else {
                    holder.receiverImage.visibility = View.GONE
                    holder.receiverName.visibility = View.GONE
                }
            }
        }
    }

    // convert timestamp to readable time - 00:00
    private fun formatDate(timestamp: Long, timezone: TimeZone = TimeZone.getDefault()): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        dateFormat.timeZone = timezone
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    private fun isMultiChat(): Boolean {
        return chatType == 2 || chatType == 3
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].user_ID == FirebaseAuth.getInstance().uid) VIEW_TYPE_SENDER
        else VIEW_TYPE_RECEIVER
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiverMessage: TextView = itemView.findViewById(R.id.receiverText)
        val receiverTime: TextView = itemView.findViewById(R.id.timeSent)
        val receiverImage: ImageView = itemView.findViewById(R.id.receiverImage)
        val receiverName: TextView = itemView.findViewById(R.id.receiverName)
    }

    class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderMessage: TextView = itemView.findViewById(R.id.senderText)
        val senderTime: TextView = itemView.findViewById(R.id.timeSent)
    }
}
