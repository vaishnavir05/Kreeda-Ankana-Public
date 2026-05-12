package com.example.kreedaankana.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.kreedaankana.databinding.FragmentAdminDashboardBinding
import com.example.kreedaankana.ui.adapters.BookingAdapter

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return AdminViewModel(
                    app.teamRepository,
                    app.groundRepository,
                    app.userRepository,
                    app.bookingRepository
                ) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val adminId = app.sessionManager.getUserId()

        // Captain Requests
        val requestAdapter = CaptainRequestAdapter(
            onApprove = { req ->
                viewModel.approveCaptainRequest(req.id, req.userId)
                Toast.makeText(requireContext(), "${req.userName} approved as Captain", Toast.LENGTH_SHORT).show()
            },
            onReject = { req ->
                viewModel.rejectCaptainRequest(req.id)
                Toast.makeText(requireContext(), "Request rejected", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvCaptainRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCaptainRequests.adapter = requestAdapter

        viewModel.getPendingCaptainRequests().observe(viewLifecycleOwner) { requests ->
            requestAdapter.submitList(requests)
            binding.tvNoCaptainRequests.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
        }

        // Team Requests
        val teamRequestAdapter = TeamRequestAdapter(
            onApprove = { req ->
                viewModel.approveTeamRequest(req.id)
                Toast.makeText(requireContext(), "Team ${req.name} approved", Toast.LENGTH_SHORT).show()
            },
            onReject = { req ->
                viewModel.rejectTeamRequest(req.id)
                Toast.makeText(requireContext(), "Team ${req.name} rejected", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvTeamRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTeamRequests.adapter = teamRequestAdapter

        viewModel.getPendingTeamRequests().observe(viewLifecycleOwner) { requests ->
            teamRequestAdapter.submitList(requests)
            binding.tvNoTeamRequests.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
        }

        // Ground Requests
        val groundRequestAdapter = GroundRequestAdapter(
            onApprove = { ground ->
                viewModel.approveGround(ground.id)
                Toast.makeText(requireContext(), "Ground ${ground.name} approved", Toast.LENGTH_SHORT).show()
            },
            onReject = { ground ->
                // viewModel.deleteGround(ground) // Need to add to VM if needed, but for now just Toast
                Toast.makeText(requireContext(), "Ground rejection not fully implemented", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvGroundRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGroundRequests.adapter = groundRequestAdapter

        viewModel.getPendingGrounds().observe(viewLifecycleOwner) { grounds ->
            groundRequestAdapter.submitList(grounds)
            binding.tvNoGroundRequests.visibility = if (grounds.isEmpty()) View.VISIBLE else View.GONE
        }

        // All Bookings Today
        val bookingAdapter = BookingAdapter()
        binding.rvAllBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllBookings.adapter = bookingAdapter
        viewModel.getTodayBookings().observe(viewLifecycleOwner) { bookings ->
            bookingAdapter.submitList(bookings)
        }

        binding.fabAddEntity.setOnClickListener {
            findNavController().navigate(R.id.action_admin_to_addEntity)
        }

        binding.btnSeedData.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                com.example.kreedaankana.utils.DataSeeder(
                    app.database,
                    app.userRepository,
                    app.teamRepository,
                    app.groundRepository,
                    app.bookingRepository,
                    app.matchRepository,
                    app.challengeRepository,
                    app.firebaseService
                ).seedAll()
                Toast.makeText(requireContext(), "Dummy data seeded & synced!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
