package com.example.kreedaankana.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedaankana.R
import com.example.kreedaankana.data.db.entity.Team

class TeamRequestAdapter(
    private val onApprove: (Team) -> Unit,
    private val onReject: (Team) -> Unit
) : ListAdapter<Team, TeamRequestAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Team>() {
            override fun areItemsTheSame(a: Team, b: Team) = a.id == b.id
            override fun areContentsTheSame(a: Team, b: Team) = a == b
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRequestUserName)
        val btnApprove: Button = view.findViewById(R.id.btnApproveRequest)
        val btnReject: Button = view.findViewById(R.id.btnRejectRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_captain_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val req = getItem(position)
        val context = holder.itemView.context
        holder.tvName.text = context.getString(R.string.format_team_request, req.name, req.sport)
        holder.btnApprove.setOnClickListener { onApprove(req) }
        holder.btnReject.setOnClickListener { onReject(req) }
    }
}
