package com.example.belajr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.R
import com.example.belajr.models.PartnerWithStatus
import com.example.belajr.models.RelationStatus

class PartnerAdapter(
    private var partners: List<PartnerWithStatus>,
    private val onActionClick: (PartnerWithStatus) -> Unit
) : RecyclerView.Adapter<PartnerAdapter.PartnerViewHolder>() {

    class PartnerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvInterests: TextView = view.findViewById(R.id.tvInterests)
        val btnAction: Button = view.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partner, parent, false)
        return PartnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartnerViewHolder, position: Int) {
        val item = partners[position]
        holder.tvUsername.text = item.profile.username
        holder.tvInterests.text = "Interests: ${item.profile.interests?.joinToString(", ") ?: "None"}"

        when (item.relationStatus) {
            RelationStatus.NONE -> {
                holder.btnAction.text = "Connect"
                holder.btnAction.isEnabled = true
            }
            RelationStatus.PENDING_OUT -> {
                holder.btnAction.text = "Requested"
                holder.btnAction.isEnabled = false
            }
            RelationStatus.PENDING_IN -> {
                holder.btnAction.text = "Accept"
                holder.btnAction.isEnabled = true
            }
            RelationStatus.FRIEND -> {
                holder.btnAction.text = "Chat"
                holder.btnAction.isEnabled = true
            }
        }

        holder.btnAction.setOnClickListener {
            onActionClick(item)
        }
    }

    override fun getItemCount() = partners.size

    fun updateData(newPartners: List<PartnerWithStatus>) {
        partners = newPartners
        notifyDataSetChanged()
    }
}