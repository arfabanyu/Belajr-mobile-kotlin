package com.example.belajr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.R

data class Subject(val name: String)

class SubjectAdapter(
    private var subjects: List<Subject>,
    private val onSubjectClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    private var selectedSubject: String = "All"

    class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvSubjectName)
        val cardSubject: CardView = view.findViewById(R.id.cardSubject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.tvName.text = subject.name

        val context = holder.itemView.context
        if (subject.name == selectedSubject) {
            holder.cardSubject.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary))
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            holder.cardSubject.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.primary))
        }

        holder.itemView.setOnClickListener {
            val oldSelected = selectedSubject
            selectedSubject = subject.name

            val oldIndex = subjects.indexOfFirst { it.name == oldSelected }
            if (oldIndex != -1) notifyItemChanged(oldIndex)
            notifyItemChanged(position)
            
            onSubjectClick(subject)
        }
    }

    override fun getItemCount() = subjects.size

    fun updateData(newSubjects: List<Subject>) {
        subjects = newSubjects
        notifyDataSetChanged()
    }
}