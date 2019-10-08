package com.example.srcwh.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.srcwh.R

class DialogActionFragment(val primary: String?, val secondary: String?, val cancel: String?, val callback: DialogActionHandler) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog_actions, container, false)

        val primary = view.findViewById<Button>(R.id.dialog_button_primary)
        val secondary = view.findViewById<Button>(R.id.dialog_button_secondary)
        val cancel = view.findViewById<Button>(R.id.dialog_button_cancel)

        primary.setOnClickListener { callback(DialogAction.PRIMARY) }
        secondary.setOnClickListener { callback(DialogAction.SECONDARY) }
        cancel.setOnClickListener { callback(DialogAction.CANCEL) }

        if (this.primary != null) {
            primary.text = this.primary
        } else {
            primary.visibility = View.GONE
        }

        if (this.secondary != null) {
            secondary.text = this.secondary
        } else {
            secondary.visibility = View.GONE
        }

        if (this.cancel != null) {
            cancel.text = this.cancel
        } else {
            cancel.visibility = View.GONE
        }

        return view
    }
}