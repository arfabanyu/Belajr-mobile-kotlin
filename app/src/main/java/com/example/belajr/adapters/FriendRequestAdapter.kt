package com.example.belajr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.R
import com.example.belajr.models.FriendRequest

class FriendRequestAdapter(
    private var requests: List<FriendRequest>,
    private val onAccept: (FriendRequest) -> Unit,
    private val onReject: (FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSenderName: TextView = view.findViewById(R.id.tvSenderName)
        val btnAccept: ImageView = view.findViewById(R.id.btnAccept)
        val btnReject: ImageView = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        // Catatan: Karena senderId adalah UUID, idealnya Anda mengambil nama dari tabel profiles
        // Untuk sementara kita tampilkan ID atau placeholder
        holder.tvSenderName.text = "User ${request.senderId.take(5)}..."

        holder.btnAccept.setOnClickListener { onAccept(request) }
        holder.btnReject.setOnClickListener { onReject(request) }
    }

    override fun getItemCount() = requests.size

    fun updateData(newRequests: List<FriendRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}