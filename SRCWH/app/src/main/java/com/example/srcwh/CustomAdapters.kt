package com.example.srcwh

import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.lesson_card.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainAdapter(val schedule:List<ClientSchedule>?): RecyclerView.Adapter<CustomViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.lesson_card, parent, false)
        return CustomViewHolder(row)
    }

    override fun getItemCount(): Int {
        return schedule!!.count()
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.view.starttime_textview.text = dateTimeFormatter(schedule?.get(position)?.start!!)
        holder.view.endtime_textview.text =  dateTimeFormatter(schedule?.get(position)?.end!!)
        holder.view.classroom_textview.text = schedule?.get(position)?.locationList?.get(0)
        holder.view.address_textview.text = "address wip"
        if(schedule?.get(position)?.attended != null) holder.view.checkmark_imageview.setImageResource(R.drawable.ic_checkbox_marked_circle_outline)
        else holder.view.checkmark_imageview.setImageResource(R.drawable.ic_radiobox_blank)
    }

    private fun dateTimeFormatter(date: LocalDateTime): String{
        val hours = if(date.hour < 10)"0" + date.hour.toString() else date.hour.toString()
        val minutes = if(date.minute < 10)"0" + date.minute.toString() else date.minute.toString()
        return  hours + ":" + minutes
    }

}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view)