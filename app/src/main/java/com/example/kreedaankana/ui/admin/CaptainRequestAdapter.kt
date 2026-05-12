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
import com.example.kreedaankana.data.db.entity.CaptainRequest

class CaptainRequestAdapter(
    private val onApprove: (CaptainRequest) -> Unit,
    private val onReject: (CaptainRequest) -> Unit
) : ListAdapter<CaptainRequest, CaptainRequestAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CaptainRequest>() {
            override fun areItemsTheSame(a: CaptainRequest, b: CaptainRequest) = a.id == b.id
            override fun areContentsTheSame(a: CaptainRequest, b: CaptainRequest) = a == b
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
        holder.tvName.text = context.getString(R.string.format_captain_request, req.userName)
        holder.btnApprove.setOnClickListener { onApprove(req) }
        holder.btnReject.setOnClickListener { onReject(req) }
    }
}
