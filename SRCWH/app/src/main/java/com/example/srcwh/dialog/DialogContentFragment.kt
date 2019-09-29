package com.example.srcwh.dialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.srcwh.R

class DialogContentFragment(val icon: Int?, val title: String, val text: String, val location: String?) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog, container, false)

        val location = view.findViewById<TextView>(R.id.dialog_location)
        val icon = view.findViewById<ImageView>(R.id.dialog_success_icon)
        val title = view.findViewById<TextView>(R.id.dialog_success_title)
        val text = view.findViewById<TextView>(R.id.dialog_success_text)

        if (this.location != null) {
            icon.visibility = View.GONE
            location.visibility = View.VISIBLE
            location.text = this.location
        } else {
            location.visibility = View.GONE
        }

        if (this.icon != null) icon.setImageResource(this.icon!!)

        title.text = this.title
        text.text = this.text

        return view
    }
}
