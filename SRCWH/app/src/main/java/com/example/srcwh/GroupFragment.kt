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

class GroupFragment(val token: String, val user: LoginUser, val callback: (group: String) -> Unit) : Fragment() {
    private lateinit var groupList: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_group, container, false)

        var groupPickList = user.groupList.map { group -> GroupAdapterItem(group, false) }

        groupList = view.findViewById(R.id.groupList)
        val groupAdapter = GroupAdapter(context!!, groupPickList)
        groupList.adapter = groupAdapter

        var button = view.findViewById<Button>(R.id.accept_button)
        button.onClick {
            var pickedGroup = groupPickList.find { item -> item.picked }

            if (pickedGroup == null) {
                Toast.makeText(context, GROUP_ERROR, Toast.LENGTH_SHORT).show()
            } else {
                Log.d("GROUP", pickedGroup!!.group)

                group_progress.visibility = View.VISIBLE
                accept_button.isEnabled = false

                val networkHandler = NetworkHandler()
                networkHandler.postGroupUpdate(token, pickedGroup!!.group) { error ->
                    if (error != null) {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    } else {
                        callback(pickedGroup!!.group)
                    }

                    group_progress.visibility = View.INVISIBLE
                    accept_button.isEnabled = true
                }
            }
        }

        return view
    }
}
