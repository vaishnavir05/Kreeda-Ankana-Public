package com.example.kreedaankana.ui.grounds

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.R
import com.example.kreedaankana.data.db.entity.Ground
import com.example.kreedaankana.databinding.DialogGroundFormBinding
import com.example.kreedaankana.databinding.FragmentGroundDetailBinding
import com.example.kreedaankana.ui.adapters.BookingAdapter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class GroundDetailFragment : Fragment() {

    private var _binding: FragmentGroundDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroundViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return GroundViewModel(app.groundRepository, app.bookingRepository) as T
            }
        }
    }

    // Holds the loaded ground so the edit button can access it
    private var currentGround: Ground? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroundDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groundId = arguments?.getInt("groundId") ?: return
        val app = requireActivity().application as KreedaApplication

        loadGround(app, groundId)

        val scheduleAdapter = BookingAdapter()
        binding.rvGroundSchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGroundSchedule.adapter = scheduleAdapter

        viewModel.getTodayScheduleForGround(groundId).observe(viewLifecycleOwner) { bookings ->
            val groundBookings = bookings.filter { it.groundId == groundId }
            scheduleAdapter.submitList(groundBookings)
            binding.tvNoDetailSchedule.visibility =
                if (groundBookings.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.btnBookThisGround.setOnClickListener {
            findNavController().navigate(R.id.action_groundDetail_to_booking)
        }

        binding.btnEditGround.setOnClickListener {
            currentGround?.let { ground -> showEditGroundDialog(app, ground) }
                ?: Toast.makeText(requireContext(), "Ground not loaded yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadGround(app: KreedaApplication, groundId: Int) {
        lifecycleScope.launch {
            val ground = app.groundRepository.getGroundById(groundId) ?: return@launch
            currentGround = ground
            binding.tvDetailGroundName.text = ground.name
            binding.tvDetailAddress.text = ground.address
            binding.tvDetailSports.text = ground.sportsSupported.replace(",", " · ")
            binding.tvDetailHours.text = "${ground.openTime} – ${ground.closeTime}"
        }
    }

    private fun showEditGroundDialog(app: KreedaApplication, ground: Ground) {
        val dialogBinding = DialogGroundFormBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            val sports = app.database.sportDao().getAllSports()
            val selectedSports = ground.sportsSupported.split(",").map { it.trim() }

            dialogBinding.chipGroupSports.removeAllViews()
            sports.forEach { sport ->
                val chip = Chip(requireContext()).apply {
                    text = sport.name
                    isCheckable = true
                    isChecked = selectedSports.contains(sport.name)
                    setChipBackgroundColorResource(R.color.selector_chip_bg)
                }
                dialogBinding.chipGroupSports.addView(chip)
            }

            // Pre-fill existing values
            dialogBinding.etGroundName.setText(ground.name)
            dialogBinding.etGroundAddress.setText(ground.address)
            dialogBinding.etOpenTime.setText(ground.openTime)
            dialogBinding.etCloseTime.setText(ground.closeTime)
            dialogBinding.etGroundDescription.setText(ground.description)

            AlertDialog.Builder(requireContext())
                .setTitle("Edit Ground")
                .setView(dialogBinding.root)
                .setPositiveButton("Save") { _, _ ->
                    val name = dialogBinding.etGroundName.text.toString().trim()
                    val address = dialogBinding.etGroundAddress.text.toString().trim()
                    val openTime = dialogBinding.etOpenTime.text.toString().trim()
                        .ifEmpty { "06:00 AM" }
                    val closeTime = dialogBinding.etCloseTime.text.toString().trim()
                        .ifEmpty { "10:00 PM" }
                    val description = dialogBinding.etGroundDescription.text.toString().trim()

                    val selectedList = mutableListOf<String>()
                    for (i in 0 until dialogBinding.chipGroupSports.childCount) {
                        val chip = dialogBinding.chipGroupSports.getChildAt(i) as? Chip
                        if (chip?.isChecked == true) selectedList.add(chip.text.toString())
                    }

                    if (name.isEmpty() || address.isEmpty()) {
                        Toast.makeText(requireContext(), "Name and address are required", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (selectedList.isEmpty()) {
                        Toast.makeText(requireContext(), "Select at least one sport", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val updated = ground.copy(
                        name = name,
                        address = address,
                        sportsSupported = selectedList.joinToString(","),
                        openTime = openTime,
                        closeTime = closeTime,
                        description = description
                    )

                    lifecycleScope.launch {
                        app.groundRepository.updateGround(updated)
                        currentGround = updated
                        // Refresh displayed values
                        binding.tvDetailGroundName.text = updated.name
                        binding.tvDetailAddress.text = updated.address
                        binding.tvDetailSports.text = updated.sportsSupported.replace(",", " · ")
                        binding.tvDetailHours.text = "${updated.openTime} – ${updated.closeTime}"
                        Toast.makeText(requireContext(), "Ground updated!", Toast.LENGTH_SHORT).show()
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
