package com.example.safedrivemonitor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Displays trip events in the Reports tab.
 */
class EventAdapter(private val events: MutableList<String>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLine: TextView = view.findViewById(R.id.tvLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.tvLine.text = events[position]
    }

    override fun getItemCount(): Int = events.size

    fun addEvent(event: String) {
        events.add(0, event)
        notifyItemInserted(0)
    }

    fun clearAll() {
        events.clear()
        notifyDataSetChanged()
    }
}
