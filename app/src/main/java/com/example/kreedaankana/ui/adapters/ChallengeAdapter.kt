package com.example.kreedaankana.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedaankana.R
import com.example.kreedaankana.data.firebase.Challenge

class ChallengeAdapter(
    private val onAccept: ((Challenge) -> Unit)? = null,
    private val onDecline: ((Challenge) -> Unit)? = null
) : ListAdapter<Challenge, ChallengeAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Challenge>() {
            override fun areItemsTheSame(a: Challenge, b: Challenge) = a.id == b.id
            override fun areContentsTheSame(a: Challenge, b: Challenge) = a == b
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMatchup: TextView = view.findViewById(R.id.tvChallengeMatchup)
        val tvSport: TextView = view.findViewById(R.id.tvChallengeSport)
        val tvGround: TextView = view.findViewById(R.id.tvChallengeGround)
        val tvTime: TextView = view.findViewById(R.id.tvChallengeTime)
        val tvStatus: TextView = view.findViewById(R.id.tvChallengeStatus)
        val btnAccept: View = view.findViewById(R.id.btnAcceptChallenge)
        val btnDecline: View = view.findViewById(R.id.btnDeclineChallenge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_challenge, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val challenge = getItem(position)
        val context = holder.itemView.context
        val opponent = if (challenge.opponentTeamId == 0) "Open Challenge" else challenge.opponentTeamName
        holder.tvMatchup.text = context.getString(R.string.format_challenge_matchup, challenge.challengerTeamName, opponent)
        holder.tvSport.text = challenge.sport
        holder.tvGround.text = challenge.groundName
        holder.tvTime.text = context.getString(R.string.format_challenge_time, challenge.proposedDate, challenge.proposedTime)
        holder.tvStatus.text = challenge.status

        // Status color
        holder.tvStatus.setBackgroundColor(
            when (challenge.status) {
                "ACCEPTED" -> Color.parseColor("#A7D6C6")
                "DECLINED" -> Color.parseColor("#FFCDD2")
                else -> Color.parseColor("#F8EDAA")
            }
        )

        // Show accept/decline only for PENDING
        val showActions = challenge.status == "PENDING" && onAccept != null
        holder.btnAccept.visibility = if (showActions) View.VISIBLE else View.GONE
        holder.btnDecline.visibility = if (showActions) View.VISIBLE else View.GONE

        holder.btnAccept.setOnClickListener { onAccept?.invoke(challenge) }
        holder.btnDecline.setOnClickListener { onDecline?.invoke(challenge) }
    }
}
