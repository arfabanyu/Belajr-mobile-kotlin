package com.example.belajr.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.R
import com.example.belajr.models.PartnerWithStatus
import com.example.belajr.models.RelationStatus
import com.google.android.material.button.MaterialButton

class PartnerAdapter(
    private var partners: List<PartnerWithStatus>,
    private val onItemClick: (PartnerWithStatus) -> Unit,
    private val onActionClick: (PartnerWithStatus) -> Unit
) : RecyclerView.Adapter<PartnerAdapter.PartnerViewHolder>() {

    class PartnerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvInterests: TextView = view.findViewById(R.id.tvInterests)
        val btnAction: MaterialButton = view.findViewById(R.id.btnAction)
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

        val context = holder.itemView.context
        val btn = holder.btnAction
        
        // Reset state
        btn.isEnabled = true
        btn.visibility = View.VISIBLE
        btn.strokeWidth = 0
        
        when (item.relationStatus) {
            RelationStatus.NONE -> {
                btn.text = "Connect"
                btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary))
                btn.setTextColor(Color.WHITE)
            }
            RelationStatus.PENDING_OUT -> {
                btn.text = "Cancel"
                // Styling: Outline merah, background transparan
                btn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                btn.strokeColor = ColorStateList.valueOf(Color.parseColor("#FF5252"))
                btn.strokeWidth = 3
                btn.setTextColor(Color.parseColor("#FF5252")) 
            }
            RelationStatus.PENDING_IN -> {
                btn.text = "Accept"
                btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                btn.setTextColor(Color.WHITE)
            }
            RelationStatus.FRIEND -> {
                btn.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        btn.setOnClickListener {
            btn.isEnabled = false // Anti klik ganda
            onActionClick(item)
        }
    }

    override fun getItemCount() = partners.size

    fun updateData(newPartners: List<PartnerWithStatus>) {
        partners = newPartners.filter { it.relationStatus != RelationStatus.FRIEND }
        notifyDataSetChanged()
    }
}
