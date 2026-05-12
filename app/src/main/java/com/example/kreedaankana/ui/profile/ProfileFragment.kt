package com.example.kreedaankana.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.R
import com.example.kreedaankana.data.db.entity.Team
import com.example.kreedaankana.data.db.entity.User
import com.example.kreedaankana.databinding.DialogEditProfileBinding
import com.example.kreedaankana.databinding.FragmentProfileBinding
import com.example.kreedaankana.ui.adapters.BookingAdapter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var currentUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val session = app.sessionManager
        val userId = session.getUserId()

        // Personal info header
        binding.tvProfileName.text = session.getUserName()
        binding.tvAvatarInitials.text = session.getUserName().take(2).uppercase()
        binding.tvProfileRole.text = session.getUserRole()

        lifecycleScope.launch {
            val user = app.userRepository.getUserById(userId)
            currentUser = user
            if (user != null) {
                binding.tvProfileEmail.text = getString(R.string.format_email, user.email)
                binding.tvProfilePhone.text = getString(R.string.format_phone, user.phone)
                binding.tvProfileSport.text = getString(R.string.format_sport, user.sport.ifEmpty { "Not set" }.replace(",", " · "))
            }

            // Team info (show all teams)
            val teams = app.teamRepository.getTeamsForUser(userId)
            binding.tvProfileTeam.text = if (teams.isNotEmpty()) {
                teams.joinToString("\n") { "${it.name} (${it.sport})" }
            } else {
                "Not in a team"
            }
        }

        // Booking history
        val bookingAdapter = BookingAdapter()
        binding.rvProfileBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProfileBookings.adapter = bookingAdapter

        app.bookingRepository.getBookingsByUser(userId).observe(viewLifecycleOwner) { bookings ->
            bookingAdapter.submitList(bookings.take(5))
            binding.tvNoBookings.visibility = if (bookings.isEmpty()) View.VISIBLE else View.GONE
        }

        // Edit Profile
        binding.btnEditProfile.setOnClickListener {
            val user = currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "Profile not loaded yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showEditProfileDialog(app, user, userId)
        }

        // Management Center button
        if (session.isCaptain() || session.isAdmin()) {
            binding.btnManagement.visibility = View.VISIBLE
            binding.btnManagement.setOnClickListener {
                findNavController().navigate(R.id.action_profile_to_addEntity)
            }
        }

        // Admin panel button
        if (session.isAdmin()) {
            binding.btnAdminPanel.visibility = View.VISIBLE
            binding.btnAdminPanel.setOnClickListener {
                findNavController().navigate(R.id.action_profile_to_admin)
            }
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            session.clearSession()
            findNavController().navigate(R.id.action_profile_to_login)
        }
    }

    private fun showEditProfileDialog(app: KreedaApplication, user: User, userId: Int) {
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)

        // Read-only fields
        dialogBinding.etEditEmail.setText(user.email)
        dialogBinding.etEditPhone.setText(user.phone)

        var allTeams: List<Team> = emptyList()
        var selectedTeamId: Int = user.teamId  // 0 means none

        lifecycleScope.launch {
            // Populate sport chips
            val sports = app.database.sportDao().getAllSports()
            val selectedSports = user.sport.split(",").map { it.trim() }
            dialogBinding.chipGroupProfileSports.removeAllViews()
            sports.forEach { sport ->
                val chip = Chip(requireContext()).apply {
                    text = sport.name
                    isCheckable = true
                    isChecked = selectedSports.contains(sport.name)
                    setChipBackgroundColorResource(R.color.selector_chip_bg)
                }
                dialogBinding.chipGroupProfileSports.addView(chip)
            }

            // Handle Team selection with a dialog instead of a spinner
            app.teamRepository.getAllTeams().observe(viewLifecycleOwner) { teams ->
                allTeams = teams
                val teamNames = mutableListOf("-- No Team --")
                teamNames.addAll(teams.map { "${it.name} (${it.sport})" })

                dialogBinding.tvSelectTeam.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Select Team")
                        .setItems(teamNames.toTypedArray()) { _, which ->
                            selectedTeamId = if (which == 0) 0 else allTeams[which - 1].id
                            dialogBinding.tvSelectTeam.text = teamNames[which]
                        }
                        .show()
                }

                // Pre-select current team (just show first one for now or last selected)
                if (user.teamId != 0) {
                    val idx = teams.indexOfFirst { it.id == user.teamId }
                    if (idx >= 0) dialogBinding.tvSelectTeam.text = teamNames[idx + 1]
                }
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(dialogBinding.root)
                .setPositiveButton("Save") { _, _ ->
                    // Get selected sports (multi-select chips)
                    val selectedSportsList = mutableListOf<String>()
                    for (i in 0 until dialogBinding.chipGroupProfileSports.childCount) {
                        val chip = dialogBinding.chipGroupProfileSports.getChildAt(i) as? Chip
                        if (chip?.isChecked == true) {
                            selectedSportsList.add(chip.text.toString())
                        }
                    }
                    val selectedSportStr = selectedSportsList.joinToString(",")

                    // Get selected team
                    val newTeamId = selectedTeamId

                    lifecycleScope.launch {
                        app.userRepository.updateSport(userId, selectedSportStr)
                        app.userRepository.updateTeam(userId, newTeamId)

                        // Refresh local state
                        val updated = app.userRepository.getUserById(userId)
                        currentUser = updated
                        if (updated != null) {
                            binding.tvProfileSport.text =
                                getString(R.string.format_sport, updated.sport.ifEmpty { "Not set" }.replace(",", " · "))
                        }

                        // Refresh team display
                        if (newTeamId != 0) {
                            val team = app.teamRepository.getTeamById(newTeamId)
                            binding.tvProfileTeam.text = if (team != null)
                                getString(R.string.format_team_name_sport, team.name, team.sport) else "Team not found"
                        } else {
                            binding.tvProfileTeam.text = "Not in a team"
                        }

                        Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
