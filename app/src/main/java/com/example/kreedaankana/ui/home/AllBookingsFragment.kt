package com.example.kreedaankana.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.databinding.FragmentAllBookingsBinding
import com.example.kreedaankana.ui.adapters.BookingAdapter

class AllBookingsFragment : Fragment() {

    private var _binding: FragmentAllBookingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KreedaApplication
        val bookingAdapter = BookingAdapter()
        binding.rvAllBookingsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllBookingsList.adapter = bookingAdapter

        app.bookingRepository.getBookingsByUser(app.sessionManager.getUserId()).observe(viewLifecycleOwner) { bookings ->
            bookingAdapter.submitList(bookings)
            binding.tvEmptyAllBookings.visibility = if (bookings.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
