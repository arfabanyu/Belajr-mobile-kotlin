package com.example.belajr.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.belajr.R
import com.example.belajr.models.PartnerResult

class OnlineFriendAdapter(
    private var friends: List<PartnerResult>,
    private val onClick: (PartnerResult) -> Unit
) : RecyclerView.Adapter<OnlineFriendAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivStoryAvatar)
        val tvName: TextView = view.findViewById(R.id.tvStoryName)
        val viewStatus: View = view.findViewById(R.id.viewOnlineStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_story_circle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.tvName.text = friend.username
        
        Glide.with(holder.itemView.context)
            .load(friend.avatarUrl)
            .placeholder(R.drawable.default_profile)
            .error(R.drawable.default_profile)
            .centerCrop()
            .into(holder.ivAvatar)

        if (friend.isOnline) {
            holder.viewStatus.setBackgroundResource(R.drawable.bg_online_dot)
        } else {
            holder.viewStatus.setBackgroundResource(R.drawable.bg_offline_dot) 
        }

        holder.itemView.setOnClickListener {
            onClick(friend)
        }
    }

    override fun getItemCount() = friends.size

    fun updateData(newFriends: List<PartnerResult>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}
