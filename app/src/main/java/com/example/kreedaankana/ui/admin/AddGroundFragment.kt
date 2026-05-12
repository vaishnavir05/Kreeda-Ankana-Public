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
import androidx.navigation.fragment.findNavController
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.databinding.FragmentAddGroundBinding

class AddGroundFragment : Fragment() {

    private var _binding: FragmentAddGroundBinding? = null
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

    private var openTime = "06:00 AM"
    private var closeTime = "10:00 PM"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddGroundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val adminId = app.sessionManager.getUserId()

        binding.btnOpenTime.setOnClickListener {
            android.app.TimePickerDialog(requireContext(), { _, h, m ->
                openTime = com.example.kreedaankana.utils.TimeUtils.formatTime12Hr(h, m)
                binding.btnOpenTime.text = openTime
            }, 6, 0, false).show()
        }

        binding.btnCloseTime.setOnClickListener {
            android.app.TimePickerDialog(requireContext(), { _, h, m ->
                closeTime = com.example.kreedaankana.utils.TimeUtils.formatTime12Hr(h, m)
                binding.btnCloseTime.text = closeTime
            }, 22, 0, false).show()
        }

        binding.btnAddGround.setOnClickListener {
            val name = binding.etGroundName.text.toString().trim()
            val address = binding.etGroundAddress.text.toString().trim()
            val sports = binding.etGroundSports.text.toString().trim()

            if (name.isEmpty() || address.isEmpty() || sports.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all ground fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val isAdmin = app.sessionManager.isAdmin()
            viewModel.addGround(name, address, sports, adminId, openTime, closeTime, isAdmin)
            Toast.makeText(requireContext(), "Ground added!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
