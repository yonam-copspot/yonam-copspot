// app/src/main/java/com/example/yonam_copspot/CommentAdapter.kt
package com.example.yonam_copspot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yonam_copspot.network.dto.CommentDto

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private val items = mutableListOf<CommentDto>()

    fun submitList(list: List<CommentDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textAuthor: TextView = itemView.findViewById(R.id.textCommentAuthor)
        private val textCreatedAt: TextView = itemView.findViewById(R.id.textCommentCreatedAt)
        private val textContent: TextView = itemView.findViewById(R.id.textCommentContent)

        fun bind(item: CommentDto) {
            textAuthor.text = item.commenterName
            textCreatedAt.text = item.createdAt
            textContent.text = item.commentText
        }
    }
}
