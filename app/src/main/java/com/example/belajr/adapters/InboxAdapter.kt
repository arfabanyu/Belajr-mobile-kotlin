package com.example.belajr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.R
import com.example.belajr.models.ChatRoom

class InboxAdapter(
    private var chatRooms: List<ChatRoom>,
    private val onClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    class InboxViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFriendName: TextView = view.findViewById(R.id.tvFriendName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return InboxViewHolder(view)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val room = chatRooms[position]
        holder.tvFriendName.text = room.friend.username
        holder.tvLastMessage.text = room.lastMessage?.content ?: "No messages yet"
        holder.tvTime.text = room.lastMessage?.sentAt?.substringAfter("T")?.substringBefore(".") ?: ""

        holder.itemView.setOnClickListener { onClick(room) }
    }

    override fun getItemCount() = chatRooms.size

    fun updateData(newRooms: List<ChatRoom>) {
        chatRooms = newRooms
        notifyDataSetChanged()
    }
}