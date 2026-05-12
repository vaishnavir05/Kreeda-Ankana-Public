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
import com.example.kreedaankana.databinding.FragmentGroundListBinding
import com.example.kreedaankana.ui.adapters.GroundAdapter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class GroundListFragment : Fragment() {

    private var _binding: FragmentGroundListBinding? = null
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroundListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = GroundAdapter { ground ->
            val bundle = Bundle().apply { putInt("groundId", ground.id) }
            findNavController().navigate(R.id.action_groundList_to_groundDetail, bundle)
        }
        binding.rvGrounds.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGrounds.adapter = adapter

        viewModel.getAllGrounds().observe(viewLifecycleOwner) { grounds ->
            adapter.submitList(grounds)
        }

        binding.fabAddGround.visibility = View.VISIBLE

        // FAB → Add new entity (consolidated)
        binding.fabAddGround.setOnClickListener {
            findNavController().navigate(R.id.action_groundList_to_addEntity)
        }
    }

    private fun showGroundFormDialog(existingGround: Ground?) {
        val app = requireActivity().application as KreedaApplication
        val dialogBinding = DialogGroundFormBinding.inflate(layoutInflater)

        // Populate sports chips from DB
        lifecycleScope.launch {
            val sports = app.database.sportDao().getAllSports()
            val selectedSports = existingGround?.sportsSupported
                ?.split(",")?.map { it.trim() } ?: emptyList()

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

            // Pre-fill fields when editing
            existingGround?.let { g ->
                dialogBinding.etGroundName.setText(g.name)
                dialogBinding.etGroundAddress.setText(g.address)
                dialogBinding.etOpenTime.setText(g.openTime)
                dialogBinding.etCloseTime.setText(g.closeTime)
                dialogBinding.etGroundDescription.setText(g.description)
            }
        }

        val title = if (existingGround == null) "Add New Ground" else "Edit Ground"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val name = dialogBinding.etGroundName.text.toString().trim()
                val address = dialogBinding.etGroundAddress.text.toString().trim()
                val openTime = dialogBinding.etOpenTime.text.toString().trim()
                    .ifEmpty { "06:00 AM" }
                val closeTime = dialogBinding.etCloseTime.text.toString().trim()
                    .ifEmpty { "10:00 PM" }
                val description = dialogBinding.etGroundDescription.text.toString().trim()

                // Gather selected sports from chips
                val selectedSports = mutableListOf<String>()
                for (i in 0 until dialogBinding.chipGroupSports.childCount) {
                    val chip = dialogBinding.chipGroupSports.getChildAt(i) as? Chip
                    if (chip?.isChecked == true) selectedSports.add(chip.text.toString())
                }

                if (name.isEmpty() || address.isEmpty()) {
                    Toast.makeText(requireContext(), "Name and address are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (selectedSports.isEmpty()) {
                    Toast.makeText(requireContext(), "Select at least one sport", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val ground = existingGround?.copy(
                    name = name,
                    address = address,
                    sportsSupported = selectedSports.joinToString(","),
                    openTime = openTime,
                    closeTime = closeTime,
                    description = description
                ) ?: Ground(
                    name = name,
                    address = address,
                    sportsSupported = selectedSports.joinToString(","),
                    openTime = openTime,
                    closeTime = closeTime,
                    description = description
                )

                lifecycleScope.launch {
                    if (existingGround == null) {
                        app.groundRepository.insertGround(ground)
                        Toast.makeText(requireContext(), "Ground added!", Toast.LENGTH_SHORT).show()
                    } else {
                        app.groundRepository.updateGround(ground)
                        Toast.makeText(requireContext(), "Ground updated!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
