package com.example.kreedaankana.ui.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.databinding.FragmentTeamBinding
import com.example.kreedaankana.ui.adapters.TeamAdapter
import androidx.navigation.fragment.findNavController
import com.example.kreedaankana.R
import com.example.kreedaankana.utils.Constants

class TeamFragment : Fragment() {

    private var _binding: FragmentTeamBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TeamViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return TeamViewModel(app.teamRepository, app.userRepository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val userId = app.sessionManager.getUserId()
        val userName = app.sessionManager.getUserName()
        val isCaptain = app.sessionManager.isCaptain()

        viewModel.loadMyTeam(userId)

        val teamAdapter = TeamAdapter { team ->
            // Join team dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Join ${team.name}?")
                .setMessage("Sport: ${team.sport}")
                .setPositiveButton("Join") { _, _ -> viewModel.joinTeam(team.id, userId) }
                .setNegativeButton("Cancel", null)
                .show()
        }
        binding.rvTeams.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTeams.adapter = teamAdapter

        viewModel.teams.observe(viewLifecycleOwner) { teams ->
            teamAdapter.submitList(teams)
        }

        viewModel.myTeam.observe(viewLifecycleOwner) { team ->
            if (team != null) {
                binding.cardMyTeamInfo.visibility = View.VISIBLE
                binding.tvMyTeamName.text = team.name
                binding.tvMyTeamSport.text = team.sport
            } else {
                binding.cardMyTeamInfo.visibility = View.GONE
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { (success, msg) ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Create Team (redirect to management center)
        binding.btnCreateTeam.setOnClickListener {
            findNavController().navigate(R.id.action_team_to_addEntity)
        }

        // Request Captain
        binding.btnRequestCaptain.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Request Captain Role")
                .setMessage("Submit a request to become a Captain? An admin will review it.")
                .setPositiveButton("Submit") { _, _ -> viewModel.requestCaptain(userId, userName) }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
