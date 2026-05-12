package com.example.kreedaankana.ui.challenge

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.R
import com.example.kreedaankana.databinding.FragmentChallengeBoardBinding
import com.example.kreedaankana.ui.adapters.ChallengeAdapter

class ChallengeBoardFragment : Fragment() {

    private var _binding: FragmentChallengeBoardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChallengeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return ChallengeViewModel(app.challengeRepository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChallengeBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val session = app.sessionManager
        val isCaptain = session.isCaptain()

        val adapter = ChallengeAdapter(
            onAccept = { challenge ->
                if (isCaptain) {
                    viewModel.acceptChallenge(challenge, session.getUserId())
                } else {
                    Toast.makeText(requireContext(), "Only Captains can accept challenges", Toast.LENGTH_SHORT).show()
                }
            },
            onDecline = { challenge ->
                viewModel.declineChallenge(challenge)
            }
        )
        binding.rvChallenges.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChallenges.adapter = adapter

        viewModel.startListening()

        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            adapter.submitList(challenges)
            binding.tvNoChallenges.visibility = if (challenges.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { (_, msg) ->
            val displayMsg = when (msg) {
                "max_match_daily" -> "Daily match limit reached for this ground"
                "max_match_weekly" -> "Weekly match limit reached"
                "overlap" -> "This slot is already booked"
                else -> msg
            }
            Toast.makeText(requireContext(), displayMsg, Toast.LENGTH_SHORT).show()
        }

        binding.btnPostChallenge.setOnClickListener {
            if (!isCaptain) {
                Toast.makeText(requireContext(), "Only Captains can post challenges", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_board_to_createChallenge)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
