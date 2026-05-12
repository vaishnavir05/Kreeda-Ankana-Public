package com.example.kreedaankana.ui.management

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
import androidx.navigation.fragment.findNavController
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.databinding.FragmentAddEntityBinding
import com.example.kreedaankana.ui.admin.AdminViewModel
import com.example.kreedaankana.ui.team.TeamViewModel
import com.example.kreedaankana.utils.Constants
import com.example.kreedaankana.utils.TimeUtils

class AddEntityFragment : Fragment() {

    private var _binding: FragmentAddEntityBinding? = null
    private val binding get() = _binding!!

    private val adminViewModel: AdminViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return AdminViewModel(app.teamRepository, app.groundRepository, app.userRepository, app.bookingRepository) as T
            }
        }
    }

    private val teamViewModel: TeamViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return TeamViewModel(app.teamRepository, app.userRepository) as T
            }
        }
    }

    private var openTime = "06:00 AM"
    private var closeTime = "10:00 PM"
    private val selectedSports = mutableListOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEntityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val userId = app.sessionManager.getUserId()
        val userName = app.sessionManager.getUserName()
        val isAdmin = app.sessionManager.isAdmin()
        val isCaptain = app.sessionManager.isCaptain()

        setupVisibility(isAdmin, isCaptain)
        setupGroundForm(userId)
        setupTeamForm(userId, userName, isCaptain)
    }

    private fun setupVisibility(isAdmin: Boolean, isCaptain: Boolean) {
        // Everyone can propose grounds and teams now
        binding.cardAddGround.visibility = View.VISIBLE
        binding.cardAddTeam.visibility = View.VISIBLE
        
        if (!isAdmin && !isCaptain) {
            binding.btnRequestCaptain.visibility = View.VISIBLE
            binding.tvTeamStatus.text = "Note: Team creation requires Admin approval. You can also request a permanent Captain role."
        }
    }

    private fun setupGroundForm(userId: Int) {
        binding.btnSelectSports.setOnClickListener {
            val sports = Constants.SPORTS_LIST.toTypedArray()
            val checkedItems = BooleanArray(sports.size) { i -> selectedSports.contains(sports[i]) }

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Supported Sports")
                .setMultiChoiceItems(sports, checkedItems) { _, which, isChecked ->
                    if (isChecked) {
                        if (!selectedSports.contains(sports[which])) selectedSports.add(sports[which])
                    } else {
                        selectedSports.remove(sports[which])
                    }
                }
                .setPositiveButton("OK") { _, _ ->
                    binding.btnSelectSports.text = if (selectedSports.isEmpty()) "Select Supported Sports" else selectedSports.joinToString(", ")
                }
                .show()
        }

        binding.btnOpenTime.setOnClickListener {
            android.app.TimePickerDialog(requireContext(), { _, h, m ->
                openTime = TimeUtils.formatTime12Hr(h, m)
                binding.btnOpenTime.text = "Open: $openTime"
            }, 6, 0, false).show()
        }

        binding.btnCloseTime.setOnClickListener {
            android.app.TimePickerDialog(requireContext(), { _, h, m ->
                closeTime = TimeUtils.formatTime12Hr(h, m)
                binding.btnCloseTime.text = "Close: $closeTime"
            }, 22, 0, false).show()
        }
        
        binding.btnAddGround.setOnClickListener {
            val name = binding.etGroundName.text.toString().trim()
            val address = binding.etGroundAddress.text.toString().trim()
            val sports = selectedSports.joinToString(",")
            val isAdmin = (requireActivity().application as KreedaApplication).sessionManager.isAdmin()

            if (name.isEmpty() || address.isEmpty() || sports.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all ground fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val isApproved = isAdmin // Admin additions are auto-approved
            adminViewModel.addGround(name, address, sports, userId, openTime, closeTime, isApproved)
            
            val msg = if (isApproved) "Ground added successfully!" else "Ground submitted for Admin approval!"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun setupTeamForm(userId: Int, userName: String, isCaptain: Boolean) {
        val adapter = ArrayAdapter(requireContext(), com.example.kreedaankana.R.layout.item_spinner_black, Constants.SPORTS_LIST)
        adapter.setDropDownViewResource(com.example.kreedaankana.R.layout.item_spinner_dropdown_black)
        binding.spinnerSports.adapter = adapter

        binding.btnCreateTeam.setOnClickListener {
            val teamName = binding.etTeamName.text.toString().trim()
            val sport = binding.spinnerSports.selectedItem.toString()
            val isAdmin = (requireActivity().application as KreedaApplication).sessionManager.isAdmin()

            if (teamName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a team name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val status = if (isAdmin) "APPROVED" else "PENDING"
            teamViewModel.createTeam(teamName, sport, userId, status)
        }

        binding.btnRequestCaptain.setOnClickListener {
            teamViewModel.requestCaptain(userId, userName)
        }

        teamViewModel.actionResult.observe(viewLifecycleOwner) { (success, msg) ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            if (success && msg.contains("created")) {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
