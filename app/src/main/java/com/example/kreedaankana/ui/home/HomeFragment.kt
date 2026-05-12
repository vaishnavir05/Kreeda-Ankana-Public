package com.example.kreedaankana.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.R
import com.example.kreedaankana.databinding.FragmentHomeBinding
import com.example.kreedaankana.ui.adapters.BookingAdapter
import com.example.kreedaankana.ui.adapters.MatchAdapter
import com.example.kreedaankana.utils.TimeUtils
import java.util.*
import java.text.SimpleDateFormat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(app.bookingRepository, app.matchRepository, app.challengeRepository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val session = app.sessionManager

        // Greeting
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good Morning! 🌅"
            hour < 17 -> "Good Afternoon! ☀️"
            else -> "Good Evening! 🌙"
        }
        binding.tvUserName.text = session.getUserName()
        binding.tvUserRole.text = session.getUserRole()

        // Today's schedule
        val bookingAdapter = BookingAdapter()
        binding.rvTodaySchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodaySchedule.adapter = bookingAdapter
        // Set Today's Date in header and Quick Action icon
        val today = Calendar.getInstance()
        binding.tvTodayDate.text = TimeUtils.formatDateDisplay(TimeUtils.todayString())
        binding.tvQuickDay.text = today.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        binding.tvQuickMonth.text = SimpleDateFormat("MMM", Locale.getDefault()).format(today.time).uppercase(Locale.ROOT)

        viewModel.getTodayBookings(session.getUserId()).observe(viewLifecycleOwner) { bookings ->
            bookingAdapter.submitList(bookings)
            binding.tvNoSchedule.visibility = if (bookings.isEmpty()) View.VISIBLE else View.GONE
        }

        // Upcoming matches
        val matchAdapter = MatchAdapter { match ->
            val bundle = Bundle().apply { putInt("matchId", match.id) }
            findNavController().navigate(R.id.matchDetailFragment, bundle)
        }
        binding.rvUpcomingMatches.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingMatches.adapter = matchAdapter

        viewModel.getUpcomingMatches().observe(viewLifecycleOwner) { matches ->
            matchAdapter.submitList(matches)
            binding.tvNoMatches.visibility = if (matches.isEmpty()) View.VISIBLE else View.GONE
        }

        // Recent Challenges
        val challengeAdapter = com.example.kreedaankana.ui.adapters.ChallengeAdapter(
            onAccept = { challenge -> 
                // Navigate to board or handle here
                findNavController().navigate(R.id.challengeBoardFragment)
            },
            onDecline = {}
        )
        binding.rvRecentChallenges.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentChallenges.adapter = challengeAdapter

        viewModel.recentChallenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.submitList(challenges)
            binding.tvNoChallenges.visibility = if (challenges.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.startListeningToChallenges()

        // Quick action clicks
        binding.cardBookSlot.setOnClickListener { findNavController().navigate(R.id.action_home_to_booking) }
        binding.cardViewGrounds.setOnClickListener { findNavController().navigate(R.id.action_home_to_groundList) }
        binding.cardMyTeam.setOnClickListener { findNavController().navigate(R.id.teamFragment) }
        binding.cardChallengeBoard.setOnClickListener { findNavController().navigate(R.id.challengeBoardFragment) }
        binding.cardAllBookings.setOnClickListener { findNavController().navigate(R.id.action_home_to_allBookings) }
        binding.cardScoreWall.setOnClickListener { findNavController().navigate(R.id.action_home_to_scoreWall) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
