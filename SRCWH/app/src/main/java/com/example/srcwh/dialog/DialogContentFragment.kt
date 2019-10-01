package com.example.srcwh.dialog

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.example.srcwh.R
import org.jetbrains.anko.textColor

class DialogContentFragment(
    val icon: Int?,
    val title: String,
    val text: String,
    val location: String?,
    val isSuccess: Boolean,
    val parentView: View?,
    val minHeight: Int?
) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog, container, false)

        val location = view.findViewById<TextView>(R.id.dialog_location)
        val icon = view.findViewById<ImageView>(R.id.dialog_success_icon)
        val loader = view.findViewById<ProgressBar>(R.id.dialog_loading)
        val title = view.findViewById<TextView>(R.id.dialog_success_title)
        val text = view.findViewById<TextView>(R.id.dialog_success_text)

        location.visibility = View.GONE
        loader.visibility = View.GONE

        when {
            this.location != null -> {
                icon.visibility = View.GONE
                location.visibility = View.VISIBLE
                location.text = this.location
            }
            this.icon == DialogConstants(context!!).ICON_LOADING -> {
                icon.visibility = View.GONE
                loader.visibility = View.VISIBLE
            }
            this.icon != null -> {
                icon.setImageResource(this.icon!!)
            }
        }

        title.text = this.title
        text.text = HtmlCompat.fromHtml(this.text, HtmlCompat.FROM_HTML_MODE_LEGACY)

        if (isSuccess) {
            val color = context!!.getColor(R.color.colorAccent)
            icon.setColorFilter(color)
            title.textColor = color
        }

        if (parentView != null) {
            parentView.minimumHeight = minHeight ?: 0
        }

        return view
    }
}
