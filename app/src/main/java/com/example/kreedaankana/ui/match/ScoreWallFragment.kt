package com.example.kreedaankana.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.databinding.FragmentScoreWallBinding
import androidx.navigation.fragment.findNavController
import com.example.kreedaankana.R
import com.example.kreedaankana.ui.adapters.ScoreAdapter

class ScoreWallFragment : Fragment() {

    private var _binding: FragmentScoreWallBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return MatchViewModel(app.matchRepository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScoreWallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val isAdmin = app.sessionManager.isAdmin()

        val adapter = ScoreAdapter(isAdmin) { match ->
            val bundle = Bundle().apply {
                putInt("matchId", match.id)
                putString("sport", match.sport)
            }
            findNavController().navigate(R.id.scoreUpdateFragment, bundle)
        }
        binding.rvScores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScores.adapter = adapter

        binding.btnAddScore.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.btnAddScore.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("matchId", -1)
                putString("sport", "")
            }
            findNavController().navigate(R.id.scoreUpdateFragment, bundle)
        }

        viewModel.getScoreboardData().observe(viewLifecycleOwner) { data ->
            adapter.submitList(data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
