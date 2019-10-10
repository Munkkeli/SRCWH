package com.example.srcwh

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_lesson.view.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri

enum class LessonState {
    ONGOING,
    ATTENDED,
    MISSED,
    UPCOMING,
}

class MainAdapter(val context: Context, var schedule: List<ClientSchedule>?) :
    RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.item_lesson, parent, false)
        return CustomViewHolder(row)
    }

    override fun getItemCount(): Int {
        return schedule?.count() ?: 0
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val lesson = schedule?.get(position)!!
        val lessonState = determineLessonState(lesson)
        val subtitle = HtmlCompat.fromHtml(
            "${lesson.code} — <b>${dateTimeFormatter(lesson.start)}</b> – ${dateTimeFormatter(lesson.end)}",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        holder.view.card_title.text = lesson.name
        holder.view.card_subtitle.text = subtitle
        holder.view.card_subtitle.contentDescription =
            "From, ${dateTimeFormatter(lesson.start)}, to, ${dateTimeFormatter(lesson.end)}"
        holder.view.card_address.text = lesson.address

        holder.view.card_location.text = HtmlCompat.fromHtml(
            lesson.locationList.joinToString("<br />"),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        holder.view.card_location.contentDescription = "Held in, ${lesson.locationList.joinToString(", or, ")}"

        val icon = when (lessonState) {
            LessonState.ATTENDED -> R.drawable.ic_checkbox_marked_circle_outline
            LessonState.MISSED -> R.drawable.ic_close_circle_outline
            else -> R.drawable.ic_radiobox_blank
        }
        holder.view.card_icon.setImageResource(icon)

        when (lessonState) {
            LessonState.ATTENDED -> {
                val color = getColor(context, R.color.colorSuccess)
                holder.view.card_icon_background.setColorFilter(color)
            }
            LessonState.ONGOING -> {
                val color = getColor(context, R.color.colorSuccess)
                holder.view.card_icon_background.setColorFilter(color)
                holder.view.card_icon_background.alpha = 0.75f
                animateIcon(context, holder.view.card_icon_background)
            }
            else -> {
                val color = getColor(context, R.color.colorAccent)
                holder.view.card_icon_background.setColorFilter(color)
                holder.view.card_icon_background.alpha = 0.75f
            }
        }

        when (lessonState) {
            LessonState.ONGOING -> {
                val color = getColor(context, R.color.colorSuccess)
                holder.view.card_state.background.setTint(color)
                holder.view.card_state.background.alpha = 192
                holder.view.card_state.text = context.getString(R.string.lesson_state_ongoing)
                animateState(context, holder.view.card_state)
            }
            LessonState.ATTENDED -> {
                val color = getColor(context, R.color.colorSuccess)
                holder.view.card_state.background.setTint(color)
                holder.view.card_state.text = context.getString(R.string.lesson_state_attended)
            }
            LessonState.MISSED -> {
                val color = getColor(context, R.color.colorAccent)
                holder.view.card_state.background.setTint(color)
                holder.view.card_state.background.alpha = 192
                holder.view.card_state.text = context.getString(R.string.lesson_state_missed)
            }
            LessonState.UPCOMING -> {
                holder.view.card_state.visibility = View.GONE
            }
        }

        if (Controller.time.isAfter(lesson.end)) {
            holder.view.alpha = 0.75f
        }

        // Open Google Maps and search for address
        holder.view.card_button_map.setOnClickListener {
            val uri =
                Uri.parse("geo:0,0?q=${lesson.address.splitToSequence(" ").joinToString("+")}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(context, intent, null)
        }
    }

    private fun dateTimeFormatter(date: ZonedDateTime): String {
        return date.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    private fun animateIcon(context: Context, view: View) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.pulse_alpha)
        view.startAnimation(anim)
    }

    private fun animateState(context: Context, view: View) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.pulse)
        view.startAnimation(anim)
    }

    private fun determineLessonState(
        lesson: ClientSchedule
    ): LessonState {
        val time = Controller.time
        return when {
            lesson.attended != null -> LessonState.ATTENDED
            lesson.start.isBefore(time) && lesson.end.isAfter(time) -> LessonState.ONGOING
            lesson.end.isBefore(time) -> LessonState.MISSED
            else -> LessonState.UPCOMING
        }
    }

}

class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view)