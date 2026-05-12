package com.example.kreedaankana.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.R
import com.example.kreedaankana.databinding.FragmentMatchDetailBinding

class MatchDetailFragment : Fragment() {

    private var _binding: FragmentMatchDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return MatchViewModel(app.matchRepository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMatchDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val matchId = arguments?.getInt("matchId") ?: return
        val app = requireActivity().application as KreedaApplication
        val isCaptain = app.sessionManager.isCaptain()

        viewModel.loadMatch(matchId)

        viewModel.currentMatch.observe(viewLifecycleOwner) { match ->
            if (match == null) return@observe
            binding.tvDetailTeams.text = "${match.teamAName} vs ${match.teamBName}"
            binding.tvDetailSport.text = match.sport
            binding.tvDetailMatchStatus.text = match.status
            binding.tvDetailGround.text = "📍 ${match.groundName}"
            binding.tvDetailDateTime.text = "🕐 ${match.dateTime}"

            if (isCaptain && match.status == "SCHEDULED") {
                binding.btnUpdateScore.visibility = View.VISIBLE
                binding.btnUpdateScore.setOnClickListener {
                    val bundle = Bundle().apply {
                        putInt("matchId", match.id)
                        putString("sport", match.sport)
                    }
                    findNavController().navigate(R.id.action_matchDetail_to_scoreUpdate, bundle)
                }
            }
        }

        viewModel.currentScore.observe(viewLifecycleOwner) { score ->
            if (score != null) {
                binding.cardScore.visibility = View.VISIBLE
                binding.tvScoreA.text = "${viewModel.currentMatch.value?.teamAName}: ${score.teamAScore}"
                binding.tvScoreB.text = "${viewModel.currentMatch.value?.teamBName}: ${score.teamBScore}"
                binding.tvWinner.text = "🏆 Winner: ${score.winner}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
