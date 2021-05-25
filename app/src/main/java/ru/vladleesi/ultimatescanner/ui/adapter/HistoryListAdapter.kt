package ru.vladleesi.ultimatescanner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.vladleesi.ultimatescanner.data.local.entity.HistoryEntity
import ru.vladleesi.ultimatescanner.databinding.RvHistoryItemBinding

class HistoryListAdapter : RecyclerView.Adapter<HistoryListAdapter.HistoryItemViewHolder>() {

    private val dataList by lazy { ArrayList<HistoryEntity>() }

    fun setData(newDataList: List<HistoryEntity>) {
        dataList.addAll(newDataList)
        notifyDataSetChanged()
    }

    fun clearData() {
        if (dataList.isNotEmpty()) {
            dataList.clear()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val binding = RvHistoryItemBinding.inflate(LayoutInflater.from(parent.context))
        return HistoryItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class HistoryItemViewHolder(private val binding: RvHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryEntity) {
            val value = "[id:${item.id}, date: ${item.date}]\n${item.type}: ${item.value}"
            binding.tvValue.text = value
        }
    }
}