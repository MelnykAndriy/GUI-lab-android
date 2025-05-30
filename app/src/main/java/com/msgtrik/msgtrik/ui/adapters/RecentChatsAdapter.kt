package com.msgtrik.msgtrik.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msgtrik.msgtrik.models.chat.RecentChat

class RecentChatsAdapter(
    private val chats: List<RecentChat>,
    private val onChatClick: (RecentChat) -> Unit
) : RecyclerView.Adapter<RecentChatsAdapter.ChatViewHolder>() {
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(android.R.id.text1)
        val lastMsgText: TextView = itemView.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.nameText.text = chat.user.profile.name
        holder.lastMsgText.text = chat.lastMessage?.content ?: "No messages yet"
        holder.itemView.setOnClickListener { onChatClick(chat) }
    }

    override fun getItemCount(): Int = chats.size
} 