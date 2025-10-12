package com.example.safedrivemonitor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Simple chat adapter for displaying messages in AssistantFragment.
 * Supports two types of messages — user and bot.
 */
class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT = 1
    }

    // ViewHolder for messages
    class MsgViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tv)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) TYPE_USER else TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == TYPE_USER)
            R.layout.item_msg_user else R.layout.item_msg_bot

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MsgViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val vh = holder as MsgViewHolder
        vh.tv.text = msg.text
        // Optional: color bubble based on risk keywords
//        when {
//            msg.text.contains("Critical", true) -> vh.tv.setBackgroundResource(R.drawable.bg_risk_critical)
//            msg.text.contains("High", true) -> vh.tv.setBackgroundResource(R.drawable.bg_risk_high)
//            msg.text.contains("Moderate", true) -> vh.tv.setBackgroundResource(R.drawable.bg_risk_moderate)
//            msg.text.contains("Caution", true) -> vh.tv.setBackgroundResource(R.drawable.bg_risk_caution)
//            msg.text.contains("Safe", true) -> vh.tv.setBackgroundResource(R.drawable.bg_risk_safe)
//            else -> vh.tv.setBackgroundResource(R.drawable.bg_risk_moderate) // default
//        }

    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }

    fun clearAll() {
        messages.clear()
        notifyDataSetChanged()
    }
}

/**
 * Data model for a chat message
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)
