package com.example.srcwh

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_group.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import android.widget.ArrayAdapter



class GroupFragment(val token: String, val user: LoginUser, val callback: (group: String) -> Unit) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_group, container, false)

        val groupList = view.findViewById<ListView>(R.id.groupList)
        val adapter = object : ArrayAdapter<String>(context, android.R.layout.simple_list_item_single_choice, user.groupList) {
            // Add contentDescription with spaced dash to help screen reader pronunciation
            override fun getView (position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = super.getView(position, convertView, parent)
                val description = user.groupList[position].split("-").joinToString(" dash ").trim()
                view.contentDescription = "${description}."
                return view
            }
        }

        groupList.adapter = adapter

        var selectedGroup: String? = null
        groupList.setOnItemClickListener { parent, view, position, id ->
            selectedGroup = user.groupList[position]
        }

        var button = view.findViewById<Button>(R.id.accept_button)
        button.onClick {
            if (selectedGroup == null) {
                Toast.makeText(context, GROUP_ERROR, Toast.LENGTH_SHORT).show()
            } else {
                Log.d("GROUP", selectedGroup)

                group_progress.visibility = View.VISIBLE
                accept_button.isEnabled = false

                val networkHandler = NetworkHandler()
                networkHandler.postGroupUpdate(token, selectedGroup!!) { error ->
                    if (error != null) {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    } else {
                        callback(selectedGroup!!)
                    }

                    group_progress.visibility = View.INVISIBLE
                    accept_button.isEnabled = true
                }
            }
        }

        return view
    }
}
