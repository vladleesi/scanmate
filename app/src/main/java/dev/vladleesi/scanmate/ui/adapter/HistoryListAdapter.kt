package dev.vladleesi.scanmate.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.vladleesi.scanmate.data.local.entity.HistoryEntity
import dev.vladleesi.scanmate.databinding.RvHistoryItemBinding

class HistoryListAdapter : RecyclerView.Adapter<HistoryListAdapter.HistoryItemViewHolder>() {

    private val dataList = arrayListOf<HistoryEntity>()

    fun setData(newDataList: List<HistoryEntity>) {
        clearData(notify = false)
        dataList.addAll(newDataList)
        notifyDataSetChanged()
    }

    fun clearData(notify: Boolean) {
        if (dataList.isNotEmpty()) {
            dataList.clear()
            if (notify) {
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val binding = RvHistoryItemBinding.inflate(LayoutInflater.from(parent.context))
        return HistoryItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    class HistoryItemViewHolder(private val binding: RvHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryEntity) {
            val value = "[id:${item.id}, date: ${item.date}]\n${item.type}: ${item.value}"
            binding.tvValue.text = value
        }
    }
}
