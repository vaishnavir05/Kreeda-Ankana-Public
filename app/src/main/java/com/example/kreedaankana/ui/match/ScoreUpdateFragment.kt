package com.example.kreedaankana.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.databinding.FragmentScoreUpdateBinding
import com.example.kreedaankana.utils.Constants

class ScoreUpdateFragment : Fragment() {

    private var _binding: FragmentScoreUpdateBinding? = null
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
        _binding = FragmentScoreUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val matchId = arguments?.getInt("matchId") ?: return
        val sport = arguments?.getString("sport") ?: ""

        if (matchId == -1) {
            binding.tilSport.visibility = View.VISIBLE
            binding.tilTeamA.visibility = View.VISIBLE
            binding.tilTeamB.visibility = View.VISIBLE
            binding.tvScoreHint.visibility = View.GONE
            binding.etTeamAScore.hint = "Team A Score"
            binding.etTeamBScore.hint = "Team B Score"
        } else {
            binding.tilSport.visibility = View.GONE
            binding.tilTeamA.visibility = View.GONE
            binding.tilTeamB.visibility = View.GONE
            val (hintA, hintB) = Constants.getScoreHint(sport)
            binding.tvScoreHint.text = "Sport: $sport\nTeam A: $hintA\nTeam B: $hintB"
            binding.etTeamAScore.hint = "Team A Score (e.g. $hintA)"
            binding.etTeamBScore.hint = "Team B Score (e.g. $hintB)"
        }

        binding.btnSubmitScore.setOnClickListener {
            val scoreA = binding.etTeamAScore.text.toString().trim()
            val scoreB = binding.etTeamBScore.text.toString().trim()
            val winner = binding.etWinner.text.toString().trim()

            if (matchId == -1) {
                val newSport = binding.etSport.text.toString().trim()
                val teamA = binding.etTeamA.text.toString().trim()
                val teamB = binding.etTeamB.text.toString().trim()
                
                if (newSport.isEmpty() || teamA.isEmpty() || teamB.isEmpty() || scoreA.isEmpty() || scoreB.isEmpty() || winner.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.createStandaloneScore(newSport, teamA, teamB, scoreA, scoreB, winner)
            } else {
                if (scoreA.isEmpty() || scoreB.isEmpty() || winner.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.submitScore(matchId, sport, scoreA, scoreB, winner)
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { (success, msg) ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            if (success) findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
