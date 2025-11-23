// app/src/main/java/com/example/yonam_copspot/ComplaintStatusAdapter.kt
package com.example.yonam_copspot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yonam_copspot.network.dto.CompletionDto

class ComplaintStatusAdapter :
    RecyclerView.Adapter<ComplaintStatusAdapter.ViewHolder>() {

    private val items = mutableListOf<CompletionDto>()

    fun submitList(list: List<CompletionDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textId: TextView = itemView.findViewById(R.id.textCompletionId)
        private val textLocation: TextView = itemView.findViewById(R.id.textCompletionLocation)
        private val textCreatedAt: TextView = itemView.findViewById(R.id.textCompletionCreatedAt)
        private val textDoneAt: TextView = itemView.findViewById(R.id.textCompletionDoneAt)
        private val textDescription: TextView =
            itemView.findViewById(R.id.textCompletionDescription)

        fun bind(item: CompletionDto) {
            textId.text = "민원 ID: ${item.id}"

            // 주소
            textLocation.text = item.locationName ?: "주소 정보 없음"

            // null 이면 "-" 표시
            val created = item.createdAt?.replace("T", " ") ?: "-"
            val done = item.doneAt?.replace("T", " ") ?: "-"

            textCreatedAt.text = "등록일시: $created"
            textDoneAt.text = "완료일시: $done"

            textDescription.text = item.description ?: "상세 내용 없음"
        }
    }
}
