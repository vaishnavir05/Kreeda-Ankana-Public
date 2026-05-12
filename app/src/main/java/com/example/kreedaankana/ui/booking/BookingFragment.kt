package com.example.kreedaankana.ui.booking

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
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.R
import com.example.kreedaankana.data.firebase.Challenge
import com.example.kreedaankana.databinding.FragmentBookingBinding
import com.example.kreedaankana.utils.Constants
import com.example.kreedaankana.utils.TimeUtils
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import java.util.*

class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private var selectedDate = TimeUtils.todayString()
    private var selectedTime = "10:00 AM"
    private var selectedGroundId = 0
    private var selectedGroundName = ""
    private var isPractice = true

    private val viewModel: BookingViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return BookingViewModel(app.bookingRepository, app.groundRepository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication

        // Sports spinner
        val sportsAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_black, Constants.SPORTS_LIST)
        sportsAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_black)
        binding.spinnerSport.adapter = sportsAdapter
        binding.spinnerSport.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                updateOpponentTeams(app)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        // Grounds spinner
        viewModel.grounds.observe(viewLifecycleOwner) { grounds ->
            val names = grounds.map { it.name }
            val groundAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_black, names)
            groundAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_black)
            binding.spinnerGround.adapter = groundAdapter
            binding.spinnerGround.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    selectedGroundId = grounds[pos].id
                    selectedGroundName = grounds[pos].name
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
        }

        // Duration spinner
        binding.rgSlotType.setOnCheckedChangeListener { _, checkedId ->
            isPractice = checkedId == R.id.rbPractice
            val isAdmin = (requireActivity().application as KreedaApplication).sessionManager.isAdmin()
            binding.llOpponent.visibility = if (isPractice) View.GONE else View.VISIBLE
            binding.llChallengerTeam.visibility = if (!isPractice && isAdmin) View.VISIBLE else View.GONE
            updateDurationSpinner()
            if (!isPractice) updateOpponentTeams(app)
        }
        updateDurationSpinner()

        // Load Opponent Teams
        updateOpponentTeams(app)

        // Date picker
        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val dialog = DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                binding.btnPickDate.text = TimeUtils.formatDateDisplay(selectedDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dialog.datePicker.minDate = System.currentTimeMillis() - 1000
            dialog.show()
        }

        // Time picker (12-hr)
        binding.btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val now = Calendar.getInstance()
                val selectedCal = Calendar.getInstance().apply {
                    val parts = selectedDate.split("-")
                    if (parts.size == 3) {
                        set(Calendar.YEAR, parts[0].toInt())
                        set(Calendar.MONTH, parts[1].toInt() - 1)
                        set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                    }
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }

                if (selectedCal.before(now)) {
                    Toast.makeText(requireContext(), "Cannot select a past time", Toast.LENGTH_SHORT).show()
                } else {
                    selectedTime = TimeUtils.formatTime12Hr(hour, minute)
                    binding.btnPickTime.text = selectedTime
                }
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }

        // Confirm booking
        binding.btnConfirmBooking.setOnClickListener {
            binding.tvBookingError.visibility = View.GONE
            val sport = binding.spinnerSport.selectedItem?.toString() ?: ""
            val slotType = if (isPractice) Constants.SLOT_PRACTICE else Constants.SLOT_MATCH
            val durationStr = binding.spinnerDuration.selectedItem?.toString() ?: "1hr"
            val duration = parseDuration(durationStr)

            // Final check: Is the selected time in the past?
            val now = Calendar.getInstance()
            val selectedCal = Calendar.getInstance().apply {
                val parts = selectedDate.split("-")
                if (parts.size == 3) {
                    set(Calendar.YEAR, parts[0].toInt())
                    set(Calendar.MONTH, parts[1].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                }
                val timeParts = TimeUtils.toMillis(selectedDate, selectedTime)
                timeInMillis = timeParts
            }

            if (selectedCal.before(now)) {
                Toast.makeText(requireContext(), "Cannot book a slot in the past", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isPractice) {
                viewModel.bookSlot(
                    userId = app.sessionManager.getUserId(),
                    groundId = selectedGroundId,
                    groundName = selectedGroundName,
                    sport = sport,
                    slotType = slotType,
                    date = selectedDate,
                    startTime = selectedTime,
                    durationMinutes = duration
                )
            } else {
                val opponentPos = binding.spinnerOpponentTeam.selectedItemPosition
                val isAdmin = app.sessionManager.isAdmin()
                
                lifecycleScope.launch {
                    val session = app.sessionManager
                    val myTeams = app.teamRepository.getTeamsForUser(session.getUserId())
                    val myTeamIds = myTeams.map { it.id }
                    val myTeamForSport = myTeams.find { it.sport == sport }
                    
                    // Admin can book for any team, or we need a challenger team.
                    // For simplicity, if Admin, we'll try to find any team of that sport to be challenger,
                    // or just require at least one team exists.
                    
                    if (!isAdmin && myTeamForSport == null) {
                        Toast.makeText(requireContext(), "You need to be in a $sport team to post a challenge", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // If admin and not in a team, we need a way to pick the challenger team.
                    // But for now, let's just allow them to post if they ARE in a team, 
                    // or maybe they should just use the Admin Dashboard.
                    // Let's at least give a better error.

                    val allApprovedTeams = app.teamRepository.getApprovedTeams().value ?: emptyList()
                    val sportsTeams = allApprovedTeams.filter { it.sport == sport }
                    val myTeamForSportFinal = if (isAdmin) {
                        val challengerPos = binding.spinnerChallengerTeam.selectedItemPosition
                        if (challengerPos >= 0 && sportsTeams.isNotEmpty()) sportsTeams[challengerPos] else null
                    } else {
                        myTeamForSport
                    }

                    if (myTeamForSportFinal == null) {
                        Toast.makeText(requireContext(), "A valid challenger team for $sport is required", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // For Opponent: All teams of that sport except challenger
                    val filteredOpponents = sportsTeams.filter { it.id != myTeamForSportFinal.id }
                    
                    val opponentTeamId = if (opponentPos == 0) 0 else filteredOpponents[opponentPos - 1].id
                    val opponentTeamName = if (opponentPos == 0) "" else filteredOpponents[opponentPos - 1].name

                    val challenge = Challenge(
                        challengerTeamId = myTeamForSportFinal.id,
                        challengerTeamName = myTeamForSportFinal.name,
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
                    val res = app.challengeRepository.postChallenge(challenge)
                    if (res.isSuccess) {
                        Toast.makeText(requireContext(), "Match Challenge Posted!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.bookingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BookingState.Loading -> binding.btnConfirmBooking.isEnabled = false
                is BookingState.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.booking_success), Toast.LENGTH_SHORT).show()
                    binding.btnConfirmBooking.isEnabled = true
                    findNavController().popBackStack()
                }
                is BookingState.Error -> {
                    binding.btnConfirmBooking.isEnabled = true
                    binding.tvBookingError.visibility = View.VISIBLE
                    binding.tvBookingError.text = when (state.message) {
                        "max_daily" -> getString(R.string.booking_limit_daily)
                        "max_weekly" -> getString(R.string.booking_limit_weekly)
                        "overlap" -> getString(R.string.booking_overlap)
                        else -> state.message
                    }
                }
                else -> binding.btnConfirmBooking.isEnabled = true
            }
        }
    }

    private fun formatDuration(mins: Int): String {
        val hours = mins / 60
        val remainingMins = mins % 60
        return if (hours == 0) "${mins}min"
        else if (remainingMins == 0) "${hours}hr"
        else "${hours}hr ${remainingMins}min"
    }

    private fun updateOpponentTeams(app: KreedaApplication) {
        val selectedSport = binding.spinnerSport.selectedItem?.toString() ?: ""
        app.teamRepository.getApprovedTeams().observe(viewLifecycleOwner) { teams ->
            val currentUserId = app.sessionManager.getUserId()
            val isAdmin = app.sessionManager.isAdmin()
            
            lifecycleScope.launch {
                val myTeams = if (isAdmin) teams else app.teamRepository.getTeamsForUser(currentUserId)
                val myTeamIds = if (isAdmin) emptyList() else myTeams.map { it.id }
                
                // Filter all teams by sport
                val sportsTeams = teams.filter { it.sport == selectedSport }
                
                // For Opponent: All teams of that sport except user's (if not admin)
                val filteredOpponents = if (isAdmin) sportsTeams else sportsTeams.filter { it.id !in myTeamIds }
                val entries = mutableListOf("🏆 OPEN CHALLENGE (Any Team)") + filteredOpponents.map { it.name }
                
                val teamAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_black, entries)
                teamAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_black)
                binding.spinnerOpponentTeam.adapter = teamAdapter

                // For Challenger (Admin only): All teams of that sport
                if (isAdmin) {
                    val challengerEntries = sportsTeams.map { it.name }
                    val challengerAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_black, challengerEntries)
                    challengerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_black)
                    binding.spinnerChallengerTeam.adapter = challengerAdapter
                    
                    if (challengerEntries.isEmpty()) {
                        Toast.makeText(requireContext(), "No approved teams for $selectedSport", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun parseDuration(str: String): Int {
        var mins = 0
        val hrMatch = Regex("(\\d+)hr").find(str)
        if (hrMatch != null) mins += hrMatch.groupValues[1].toInt() * 60
        val minMatch = Regex("(\\d+)min").find(str)
        if (minMatch != null) mins += minMatch.groupValues[1].toInt()
        return if (mins == 0) 60 else mins
    }

    private fun updateDurationSpinner() {
        val durations = if (isPractice) Constants.PRACTICE_DURATIONS else Constants.MATCH_DURATIONS
        val labels = durations.map { formatDuration(it) }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_black, labels)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown_black)
        binding.spinnerDuration.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
