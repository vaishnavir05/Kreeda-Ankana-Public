package com.example.kreedaankana.ui.challenge

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.kreedaankana.R
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.data.firebase.Challenge
import com.example.kreedaankana.databinding.FragmentCreateChallengeBinding
import com.example.kreedaankana.utils.Constants
import com.example.kreedaankana.utils.TimeUtils
import kotlinx.coroutines.launch
import java.util.*

class CreateChallengeFragment : Fragment() {

    private var _binding: FragmentCreateChallengeBinding? = null
    private val binding get() = _binding!!

    private var selectedDate = TimeUtils.todayString()
    private var selectedTime = "10:00 AM"
    private var selectedGroundId = 0
    private var selectedGroundName = ""

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
        _binding = FragmentCreateChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val session = app.sessionManager

        // Sports
        val sportsAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner_black, Constants.SPORTS_LIST)
        binding.spinnerChallengeSport.adapter = sportsAdapter

        // Grounds
        app.groundRepository.getApprovedGrounds().observe(viewLifecycleOwner) { grounds ->
            val names = grounds.map { it.name }
            val groundAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner_black, names)
            binding.spinnerChallengeGround.adapter = groundAdapter
            binding.spinnerChallengeGround.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    selectedGroundId = grounds[pos].id
                    selectedGroundName = grounds[pos].name
                }
                override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
            })
        }

        // Teams (opponent)
        var approvedTeamsList: List<com.example.kreedaankana.data.db.entity.Team> = emptyList()
        app.teamRepository.getApprovedTeams().observe(viewLifecycleOwner) { teams ->
            approvedTeamsList = teams
            val entries = mutableListOf("Open Challenge (any team)") + teams.map { it.name }
            val teamAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner_black, entries)
            binding.spinnerOpponentTeam.adapter = teamAdapter
        }

        // Date picker
        binding.btnChallengeDatePicker.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                binding.btnChallengeDatePicker.text = TimeUtils.formatDateDisplay(selectedDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Time picker
        binding.btnChallengeTimePicker.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedTime = TimeUtils.formatTime12Hr(hour, minute)
                binding.btnChallengeTimePicker.text = selectedTime
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }

        // Submit
        binding.btnSubmitChallenge.setOnClickListener {
            val sport = binding.spinnerChallengeSport.selectedItem?.toString() ?: ""
            val opponentPos = binding.spinnerOpponentTeam.selectedItemPosition

            lifecycleScope.launch {
                val myTeams = app.teamRepository.getTeamsForUser(session.getUserId())
                val myTeam = myTeams.find { it.sport == sport }

                if (myTeam == null) {
                    Toast.makeText(requireContext(), "You need to be in a $sport team to post challenges", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val opponentTeamId = if (opponentPos == 0) 0 else approvedTeamsList[opponentPos - 1].id
                val opponentTeamName = if (opponentPos == 0) "" else approvedTeamsList[opponentPos - 1].name

                val challenge = Challenge(
                    challengerTeamId = myTeam.id,
                    challengerTeamName = myTeam.name,
                    opponentTeamId = opponentTeamId,
                    opponentTeamName = opponentTeamName,
                    sport = sport,
                    groundId = selectedGroundId,
                    groundName = selectedGroundName,
                    proposedTime = selectedTime,
                    proposedDate = selectedDate,
                    createdAt = System.currentTimeMillis(),
                    createdBy = session.getUserId().toString()
                )
                viewModel.postChallenge(challenge)
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
