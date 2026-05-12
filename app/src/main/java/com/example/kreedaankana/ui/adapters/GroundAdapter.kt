package com.example.kreedaankana.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedaankana.R
import com.example.kreedaankana.data.db.entity.Ground

class GroundAdapter(
    private val onItemClick: (Ground) -> Unit
) : ListAdapter<Ground, GroundAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Ground>() {
            override fun areItemsTheSame(a: Ground, b: Ground) = a.id == b.id
            override fun areContentsTheSame(a: Ground, b: Ground) = a == b
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvGroundName)
        val tvAddress: TextView = view.findViewById(R.id.tvGroundAddress)
        val tvSports: TextView = view.findViewById(R.id.tvGroundSports)
        val tvHours: TextView = view.findViewById(R.id.tvGroundHours)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ground, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ground = getItem(position)
        holder.tvName.text = ground.name
        holder.tvAddress.text = ground.address
        holder.tvSports.text = "Sports: ${ground.sportsSupported.replace(",", " · ")}"
        holder.tvHours.text = "${ground.openTime} – ${ground.closeTime}"
        holder.itemView.setOnClickListener { onItemClick(ground) }
    }
}
