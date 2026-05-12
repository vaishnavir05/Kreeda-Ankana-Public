package com.example.kreedaankana.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedaankana.data.db.entity.Match
import com.example.kreedaankana.data.db.entity.Score
import com.example.kreedaankana.databinding.ItemScoreBinding
import java.text.SimpleDateFormat
import java.util.*

class ScoreAdapter(private val isAdmin: Boolean, private val onUpdateClick: (Match) -> Unit) : ListAdapter<Pair<Match, Score>, ScoreAdapter.ScoreViewHolder>(ScoreDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val binding = ItemScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScoreViewHolder(binding, isAdmin, onUpdateClick)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ScoreViewHolder(private val binding: ItemScoreBinding, private val isAdmin: Boolean, private val onUpdateClick: (Match) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pair: Pair<Match, Score>) {
            val (match, score) = pair
            binding.tvSportLabel.text = match.sport.uppercase()
            binding.tvTeamAName.text = match.teamAName
            binding.tvTeamBName.text = match.teamBName
            binding.tvTeamAScore.text = score.teamAScore
            binding.tvTeamBScore.text = score.teamBScore
            binding.tvWinner.text = if (score.winner.isNotEmpty()) "Winner: ${score.winner}" else "Pending Result"
            binding.tvGroundName.text = "at ${match.groundName}"
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(score.recordedAt))

            // Option to update scores for matches that are "over" (started/finished)
            val isOver = System.currentTimeMillis() > match.dateTimeMillis
            binding.btnUpdateScore.visibility = if (isAdmin && isOver) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnUpdateScore.text = if (score.id == 0) "Add Score" else "Update Score"
            binding.btnUpdateScore.setOnClickListener { onUpdateClick(match) }
        }
    }

    class ScoreDiffCallback : DiffUtil.ItemCallback<Pair<Match, Score>>() {
        override fun areItemsTheSame(oldItem: Pair<Match, Score>, newItem: Pair<Match, Score>): Boolean {
            return oldItem.second.id == newItem.second.id
        }

        override fun areContentsTheSame(oldItem: Pair<Match, Score>, newItem: Pair<Match, Score>): Boolean {
            return oldItem == newItem
        }
    }
}
