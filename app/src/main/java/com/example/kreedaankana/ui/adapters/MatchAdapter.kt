package com.example.kreedaankana.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedaankana.R
import com.example.kreedaankana.data.db.entity.Match

class MatchAdapter(
    private val onItemClick: (Match) -> Unit
) : ListAdapter<Match, MatchAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Match>() {
            override fun areItemsTheSame(a: Match, b: Match) = a.id == b.id
            override fun areContentsTheSame(a: Match, b: Match) = a == b
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeams: TextView = view.findViewById(R.id.tvMatchTeams)
        val tvSport: TextView = view.findViewById(R.id.tvMatchSport)
        val tvGround: TextView = view.findViewById(R.id.tvMatchGround)
        val tvDateTime: TextView = view.findViewById(R.id.tvMatchDateTime)
        val tvStatus: TextView = view.findViewById(R.id.tvMatchStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val match = getItem(position)
        holder.tvTeams.text = "${match.teamAName} vs ${match.teamBName}"
        holder.tvSport.text = match.sport
        holder.tvGround.text = match.groundName
        holder.tvDateTime.text = match.dateTime
        holder.tvStatus.text = match.status
        holder.itemView.setOnClickListener { onItemClick(match) }
    }
}
