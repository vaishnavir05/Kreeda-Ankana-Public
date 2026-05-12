package com.example.kreedaankana.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedaankana.R
import com.example.kreedaankana.data.db.entity.Team

class TeamAdapter(
    private val onItemClick: (Team) -> Unit
) : ListAdapter<Team, TeamAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Team>() {
            override fun areItemsTheSame(a: Team, b: Team) = a.id == b.id
            override fun areContentsTheSame(a: Team, b: Team) = a == b
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeamName: TextView = view.findViewById(R.id.tvTeamName)
        val tvTeamSport: TextView = view.findViewById(R.id.tvTeamSport)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val team = getItem(position)
        holder.tvTeamName.text = team.name
        holder.tvTeamSport.text = team.sport
        holder.itemView.setOnClickListener { onItemClick(team) }
    }
}
