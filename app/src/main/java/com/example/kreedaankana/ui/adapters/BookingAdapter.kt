package com.example.kreedaankana.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedaankana.R
import com.example.kreedaankana.data.db.entity.Booking

class BookingAdapter : ListAdapter<Booking, BookingAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Booking>() {
            override fun areItemsTheSame(a: Booking, b: Booking) = a.id == b.id
            override fun areContentsTheSame(a: Booking, b: Booking) = a == b
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGround: TextView = view.findViewById(R.id.tvBookingGround)
        val tvSport: TextView = view.findViewById(R.id.tvBookingSport)
        val tvTime: TextView = view.findViewById(R.id.tvBookingTime)
        val tvType: TextView = view.findViewById(R.id.tvBookingType)
        val tvStatus: TextView = view.findViewById(R.id.tvBookingStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = getItem(position)
        holder.tvGround.text = booking.groundName
        holder.tvSport.text = booking.sport
        val context = holder.itemView.context
        holder.tvTime.text = context.getString(R.string.format_booking_time, booking.startTime, booking.endTime)
        holder.tvType.text = booking.slotType
        holder.tvStatus.text = booking.status
    }
}
