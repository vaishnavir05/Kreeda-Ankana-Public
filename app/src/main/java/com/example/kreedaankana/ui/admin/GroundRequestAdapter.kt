package com.example.kreedaankana.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedaankana.data.db.entity.Ground
import com.example.kreedaankana.databinding.ItemGroundRequestBinding

class GroundRequestAdapter(
    private val onApprove: (Ground) -> Unit,
    private val onReject: (Ground) -> Unit
) : ListAdapter<Ground, GroundRequestAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGroundRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemGroundRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Ground) {
            binding.tvGroundName.text = item.name
            binding.tvAddress.text = item.address
            binding.tvSports.text = "Sports: ${item.sportsSupported}"
            binding.btnApprove.setOnClickListener { onApprove(item) }
            binding.btnReject.setOnClickListener { onReject(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Ground>() {
        override fun areItemsTheSame(oldItem: Ground, newItem: Ground) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Ground, newItem: Ground) = oldItem == newItem
    }
}
