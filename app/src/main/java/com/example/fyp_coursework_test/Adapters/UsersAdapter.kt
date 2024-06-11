package com.example.fyp_coursework_test.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_coursework_test.Activities.ChatDetailsActivity
import com.example.fyp_coursework_test.Models.UserModel
import com.example.fyp_coursework_test.R
import android.text.Html
import android.util.Log
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UsersAdapter(private val context: Context,
                   private val userList: ArrayList<UserModel>,
                    private val usage: Int) :
    RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    companion object {
        const val USAGE_CHATS = 1
        const val USAGE_ADMIN = 2
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.sampleUserProfileImage)
        val fullName: TextView = itemView.findViewById(R.id.sampleUserUsernameText)
        val lastMessage: TextView = itemView.findViewById(R.id.sampleUserLastMsgText)
        val lastMessageTime: TextView = itemView.findViewById(R.id.sampleUserLastMsgTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_show_user, parent, false)
        return ViewHolder(view)
    }

    // bind all user data to the layout
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        Picasso.get().load(user.profile_Pic)
            .placeholder(R.drawable.profile_circle_svgrepo_com)
            .into(holder.profileImage)
        holder.fullName.text = user.username

        FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
            .child("Chats")
            .child("Direct")
            .child(FirebaseAuth.getInstance().currentUser!!.uid + user.user_ID)
            .orderByChild("timestamp")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (messageSnapshot in snapshot.children) {
                            val lastMessage = messageSnapshot.child("message").getValue(String::class.java)
                            val lastMessageTime = messageSnapshot.child("timestamp").getValue(Long::class.java)
                            Log.d("lastMessageTime", lastMessageTime.toString())

                            if (lastMessage != null) {
                                holder.lastMessage.text = lastMessage
                                holder.lastMessageTime.text = formatDate(lastMessageTime!!)

                            } else {
                                holder.lastMessage.text = "<i>No Message</i>"
                                holder.lastMessage.text = Html.fromHtml(holder.lastMessage.text.toString())
                                holder.lastMessageTime.text = ""
                            }
                        }
                    } else {
                        holder.lastMessage.text = "<i>No Message</i>"
                        holder.lastMessage.text = Html.fromHtml(holder.lastMessage.text.toString())
                        holder.lastMessageTime.text = ""


                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })


        holder.itemView.setOnClickListener {
            pressUser(holder, user)
        }
    }

    // set the click listener for the user layout
    private fun pressUser(holder: ViewHolder, user: UserModel) {
        when (usage){

            // if the adapter is used in admin page, delete the user after a confirmation
            USAGE_ADMIN -> {
                val builder = AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("Confirm Deletion")
                builder.setMessage("Are you sure you want to delete this user?")

                builder.setPositiveButton("Yes") { dialog, _ ->
                    deleteUser(holder.itemView.context, user)
                    dialog.dismiss()
                }

                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            }

            // If adapter used in Chats Fragment, open up chat details activity
            USAGE_CHATS -> {
                val intent = Intent(holder.itemView.context, ChatDetailsActivity::class.java)

                intent.putExtra("userID", user.user_ID)
                intent.putExtra("profilePic", user.profile_Pic)
                intent.putExtra("username", user.username)
                intent.putExtra("chatType", 1)

                holder.itemView.context.startActivity(intent)}
        }
    }

    // delete user details from database and storage
    private fun deleteUser(context: Context, user: UserModel) {

                // Delete user from database
                FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")
                    .reference
                    .child("Users")
                    .child(user.user_ID)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "User deleted from database", Toast.LENGTH_SHORT).show()

                        // Delete user's profile image folder from storage
                        val storageRef = FirebaseStorage.getInstance().reference
                        val profileImageRef = storageRef.child("profile_images/${user.user_ID}")

                        profileImageRef.delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Profile image folder deleted from storage", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(context, "Failed to delete profile image folder from storage: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Failed to delete user from database: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }

    }

    // convert timestamp to readable time - 00:00
    private fun formatDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val messageTime = Calendar.getInstance()
        messageTime.timeInMillis = timestamp

        // If the message was sent today
        if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return dateFormat.format(Date(timestamp))
        }

        // If the message was sent yesterday
        now.add(Calendar.DAY_OF_YEAR, -1)
        if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday"
        }

        // dates before yesterday
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }


    override fun getItemCount(): Int {
        return userList.size
    }
}