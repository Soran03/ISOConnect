package com.example.fyp_coursework_test.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_coursework_test.Activities.MainActivity
import com.example.fyp_coursework_test.Models.IsocModel
import com.example.fyp_coursework_test.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class JoinIsocsAdapter(private val context: Context,
                       private val isocList: ArrayList<IsocModel>,
                       private val database: FirebaseDatabase) :
    RecyclerView.Adapter<JoinIsocsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.joinIsocImage)
        val isocName: TextView = itemView.findViewById(R.id.joinIsocNameText)
        val totalMembers: TextView = itemView.findViewById(R.id.joinTotalMembers)
        val joinBtn: TextView = itemView.findViewById(R.id.joinBtn)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoinIsocsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_join_new_isoc, parent, false)
        return ViewHolder(view)
    }

    // bind ISOC data to the layout
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isoc = isocList[position]

        Picasso.get().load(isoc.profilePic)
            .placeholder(R.drawable.ic_baseline_account_circle_24_offwhite)
            .into(holder.profileImage)
        holder.isocName.text = isoc.isocName

        holder.joinBtn.setOnClickListener {
            pressJoin(holder, isoc.isocId)
        }
    }

    override fun getItemCount(): Int {
        return isocList.size
    }

    // join the corresponding Isoc's member list
    private fun pressJoin(holder: JoinIsocsAdapter.ViewHolder, isocId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val isocReference = database.reference.child("Isocs").child(isocId)

            isocReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val isoc = dataSnapshot.getValue(IsocModel::class.java)

                    if (isoc != null) {
                        val updatedMembers = isoc.memberList.toMutableList()
                        updatedMembers.add(userId)
                        isocReference.child("memberList").setValue(updatedMembers)
                        Toast.makeText(context, "Joined ${isoc.isocName}", Toast.LENGTH_SHORT).show()

                        val intent = Intent(holder.itemView.context, MainActivity::class.java)
                        holder.itemView.context.startActivity(intent)
//                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }


}