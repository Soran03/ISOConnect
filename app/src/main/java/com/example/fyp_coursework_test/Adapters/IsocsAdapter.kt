package com.example.fyp_coursework_test.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_coursework_test.Activities.AnnouncementsActivity
import com.example.fyp_coursework_test.Activities.ChatDetailsActivity
import com.example.fyp_coursework_test.Models.IsocModel
import com.example.fyp_coursework_test.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class IsocsAdapter(private val context: Context, private val isocList: ArrayList<IsocModel>) :
    RecyclerView.Adapter<IsocsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.sampleIsocImage)
        val isocName: TextView = itemView.findViewById(R.id.sampleIsocName)
        val totalMembers: TextView = itemView.findViewById(R.id.sampleIsocMembersNumber)
        val announcementsBtn: TextView = itemView.findViewById(R.id.sampleIsocAnnouncementsButton)
        val groupChatBtn: TextView = itemView.findViewById(R.id.sampleIsocGroupchatButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_show_isoc, parent, false)
        return ViewHolder(view)
    }

    // bind all the isoc data to the layout
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isoc = isocList[position]

        Picasso.get().load(isoc.profilePic)
            .placeholder(R.drawable.profile_circle_svgrepo_com)
            .into(holder.profileImage)
        holder.isocName.text = isoc.isocName

        FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
            .child("Isocs")
            .child(isoc.isocId)
            .child("memberList")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totalMembersCount = snapshot.childrenCount.toInt()
                    holder.totalMembers.text = totalMembersCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })

        // navigate to correct announcements activity
        holder.announcementsBtn.setOnClickListener {
            pressAnnouncements(holder, isoc)
        }

        // navigate to correct chat details activity
        holder.groupChatBtn.setOnClickListener {
            pressGroupChat(holder, isoc)
        }
    }

    // navigate to correct chat details activity
    private fun pressGroupChat(holder: IsocsAdapter.ViewHolder, isoc: IsocModel) {
        val intent = Intent(holder.itemView.context, ChatDetailsActivity::class.java)
        intent.putExtra("isocId", isoc.isocId)
        intent.putExtra("profilePic", isoc.profilePic)
        intent.putExtra("isocName", isoc.isocName)
        intent.putExtra("chatType", 3)

        holder.itemView.context.startActivity(intent)
    }

    // navigate to correct announcements activity
    private fun pressAnnouncements(holder: IsocsAdapter.ViewHolder, isoc: IsocModel) {
        val intent = Intent(holder.itemView.context, AnnouncementsActivity::class.java)
        intent.putExtra("isocId", isoc.isocId)
        intent.putExtra("profilePic", isoc.profilePic)
        intent.putExtra("isocName", isoc.isocName)

        holder.itemView.context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return isocList.size
    }
}