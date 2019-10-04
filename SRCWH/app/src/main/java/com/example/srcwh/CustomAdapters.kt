package com.example.srcwh

import android.content.Context
import android.icu.text.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.lesson_card.view.*
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainAdapter(val context: Context, val schedule: List<ClientSchedule>?) : RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.lesson_card, parent, false)
        return CustomViewHolder(row)
    }

    override fun getItemCount(): Int {
        return schedule?.count() ?: 0
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val startTime = schedule?.get(position)?.start!!
        val endTime = schedule?.get(position)?.end

        holder.view.starttime_textview.text = dateTimeFormatter(startTime)
        holder.view.endtime_textview.text = dateTimeFormatter(endTime)
        holder.view.classroom_textview.text = schedule?.get(position)?.locationList?.get(0)
        holder.view.address_textview.text = "address wip"

        val top_right_icon = cardTopRightIcon(endTime, position)
        holder.view.checkmark_imageview.setImageResource(top_right_icon)

        if (top_right_icon == R.drawable.ic_checkbox_marked_circle) {
            val color = getColor(context!!, R.color.colorSuccess)
            holder.view.checkmark_imageview.setColorFilter(color)
            // holder.view.card_view.alpha = 0.75f
            // holder.view.card_view.elevation = 0f
        }

        val lectureStateImage = determineLessonState(endTime, startTime, position)
        if (lectureStateImage != null) holder.view.lesson_state_imageview.setImageResource(
            lectureStateImage
        )
        if (lectureStateImage == R.drawable.card_ongoing_blue_icon) animateOngoing(
            holder.view.context,
            holder.view.lesson_state_imageview
        )
    }

    private fun dateTimeFormatter(date: ZonedDateTime ): String {
        return date.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    private fun animateOngoing(context: Context, view: View) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.pulse)
        view.startAnimation(anim)
    }

    private fun cardTopRightIcon(endTime: ZonedDateTime, position: Int): Int {
        val time = ZonedDateTime.now()
        if (time.isAfter(endTime)) {
            if (schedule?.get(position)?.attended != null) return R.drawable.ic_checkbox_marked_circle
            else return R.drawable.ic_close_circle_outline
        } else return R.drawable.ic_radiobox_blank
    }

    private fun determineLessonState(
        endTime: ZonedDateTime,
        startTime: ZonedDateTime,
        lecture: Int
    ): Int? {
        // this function get's called to check if the lesson is
        // a) attended
        // b) missed
        // c) ongoing
        val time = ZonedDateTime.now()
        if (time.isAfter(endTime)) {
            // if the current time is after the lecture ending time
            // check if the user has attended and return
            if (schedule?.get(lecture)?.attended != null) return R.drawable.card_attended_green_icon else return R.drawable.card_missed_yellow_icon
        } else {
            // the current time is before the lecture ending time, meaning
            // the lecture is either ongoing or in the future
            if (time.isAfter(startTime)) {
                return R.drawable.card_ongoing_blue_icon
            }
            // if none of the conditions are met
            return null
        }
    }

}

class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)