package com.example.belajr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PartnerAdapter(private var partners: List<Profile>) :
    RecyclerView.Adapter<PartnerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.tvUsername)
        val interests: TextView = view.findViewById(R.id.tvInterests)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partner = partners[position]
        holder.username.text = partner.username
        holder.interests.text = partner.interests?.joinToString(", ") ?: "Belum ada subject"
    }

    override fun getItemCount() = partners.size

    fun updateData(newPartners: List<Profile>) {
        partners = newPartners
        notifyDataSetChanged()
    }
}