package com.example.fyp_coursework_test.Adapters

import android.content.Context
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_coursework_test.Models.AnnouncementModel
import com.example.fyp_coursework_test.Models.IsocModel
import com.example.fyp_coursework_test.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AnnouncementAdapter(
    private val context: Context,
    private val announcementList: ArrayList<AnnouncementModel>
) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var isExpanded = false // State flag for each ViewHolder

        private val titleCol: TextView = itemView.findViewById(R.id.announcementTitleCol)
        private val descriptionCol: TextView = itemView.findViewById(R.id.descriptionCol)
        private val timeSentCol: TextView = itemView.findViewById(R.id.announcementTimeSentCol)
        private val imageCol: ImageView = itemView.findViewById(R.id.announcementImageCol)
        private val announcementLayoutCol: LinearLayout = itemView.findViewById(R.id.announcementLayoutCol)

        private val adminName: TextView = itemView.findViewById(R.id.adminName)
        private val adminImage: ImageView = itemView.findViewById(R.id.adminImage)
        private val titleExp: TextView = itemView.findViewById(R.id.announcementTitleExp)
        private val descriptionExp: TextView = itemView.findViewById(R.id.announcementDescriptionExp)
        private val timeSentExp: TextView = itemView.findViewById(R.id.announcementTimeSentExp)
        private val imageExp: ImageView = itemView.findViewById(R.id.announcementImageExp)
        private val announcementLayoutExp: LinearLayout = itemView.findViewById(R.id.announcementLayoutExp)

        private val closeExpBtn: ImageView = itemView.findViewById(R.id.closeExpBtn)

        init {
            //expand announcement layout
            announcementLayoutCol.setOnClickListener {
                toggleExpandedState()
            }

            //expand announcement layout
            closeExpBtn.setOnClickListener {
                toggleExpandedState()
            }
        }

        private fun toggleExpandedState() {
            isExpanded = !isExpanded
            announcementLayoutExp.visibility = if (isExpanded) View.VISIBLE else View.GONE
            announcementLayoutCol.visibility = if (isExpanded) View.GONE else View.VISIBLE
            TransitionManager.beginDelayedTransition(itemView as ViewGroup)
            Log.d("AnnouncementAdapter", "Toggled expanded state for position $adapterPosition: $isExpanded")
        }


        fun bind(announcement: AnnouncementModel) {
            announcementLayoutExp.visibility = if (isExpanded) View.VISIBLE else View.GONE
            announcementLayoutCol.visibility = if (!isExpanded) View.VISIBLE else View.GONE

            // bind all the announcement data into the layout
            titleCol.text = announcement.announcementTitle
            descriptionCol.text = announcement.description
            Picasso.get().load(announcement.imageUrl)
                .placeholder(R.drawable.ic_baseline_account_circle_24_offwhite)
                .into(imageCol)
            timeSentCol.text = formatDate(announcement.timestamp, TimeZone.getDefault())

            titleExp.text = announcement.announcementTitle
            descriptionExp.text = announcement.description
            Picasso.get().load(announcement.imageUrl)
                .placeholder(R.drawable.ic_baseline_account_circle_24_offwhite)
                .into(imageExp)
            timeSentExp.text = formatDate(announcement.timestamp, TimeZone.getDefault())

            // retrieve admins name from firebase
            FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")
                .reference
                .child("Users")
                .child(announcement.adminId)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.child("username").getValue(String::class.java)

                        adminName.text = username.toString()

                        val profilePicUrl = snapshot.child("profile_Pic").getValue(String::class.java)
                        Picasso.get().load(profilePicUrl)
                            .placeholder(R.drawable.ic_baseline_account_circle_24_offwhite)
                            .into(adminImage)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }

    }

    // convert timestamp to readable time - 00:00
    private fun formatDate(timestamp: Long, timezone: TimeZone = TimeZone.getDefault()): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        dateFormat.timeZone = timezone
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sample_announcement_msg, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(announcementList[position])
    }

    override fun getItemCount(): Int = announcementList.size
}
