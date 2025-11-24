// app/src/main/java/com/example/yonam_copspot/MyComplaintsAdapter.kt
package com.example.yonam_copspot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.yonam_copspot.ui.MyComplaintUiModel

class MyComplaintsAdapter(
    private val onItemClick: (MyComplaintUiModel) -> Unit
) : RecyclerView.Adapter<MyComplaintsAdapter.ViewHolder>() {

    private val items = mutableListOf<MyComplaintUiModel>()

    fun submitList(list: List<MyComplaintUiModel>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_complaint, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        itemView: View,
        private val onItemClick: (MyComplaintUiModel) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val textLocation: TextView = itemView.findViewById(R.id.textMyLocation)
        private val textStatus: TextView = itemView.findViewById(R.id.textMyStatus)
        private val textCreatedAt: TextView = itemView.findViewById(R.id.textMyCreatedAt)
        private val textDoneAt: TextView = itemView.findViewById(R.id.textMyDoneAt)
        private val textDescription: TextView = itemView.findViewById(R.id.textMyDescription)
        private val textCommentCount: TextView = itemView.findViewById(R.id.textMyCommentCount)

        fun bind(item: MyComplaintUiModel) {
            textLocation.text = item.locationName ?: "장소 없음"
            textDescription.text = item.description ?: "상세 내용 없음"

            // 등록/완료 시각은 일단 raw string 그대로 표시
            textCreatedAt.text = "등록일시: ${item.createdAt}"
            textDoneAt.text = "완료일시: ${item.doneAt ?: "-"}"

            // 완료 여부 표시
            val context = itemView.context
            if (item.isDone) {
                textStatus.text = "완료"
                textStatus.setTextColor(
                    ContextCompat.getColor(context, android.R.color.holo_green_light)
                )
            } else {
                textStatus.text = "진행중"
                textStatus.setTextColor(
                    ContextCompat.getColor(context, android.R.color.holo_orange_light)
                )
            }

            // 댓글 개수 표시
            textCommentCount.text = "댓글 ${item.commentCount}개"

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
