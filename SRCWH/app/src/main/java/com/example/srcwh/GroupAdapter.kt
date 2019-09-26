package com.example.srcwh

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.group_list_item.view.*
import android.widget.Toast



class GroupAdapterItem(val group: String, var picked: Boolean)

class GroupAdapter(private val context: Context, private val groupList: List<GroupAdapterItem>) : BaseAdapter() {
    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return groupList.size
    }

    override fun getItem(position: Int): Any {
        return groupList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun handleSelect(item: GroupAdapterItem) {
        groupList.map { item -> item.picked = false }
        item.picked = true

        this.notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = groupList[position]
        val groupItemView = inflater.inflate(R.layout.group_list_item, parent, false)

        groupItemView.groupNameText.text = item.group
        groupItemView.groupSelected.isChecked = item.picked

        groupItemView.groupSelected.setOnClickListener {
            handleSelect(item)
        }

        groupItemView.setOnClickListener {
            handleSelect(item)
        }

        return groupItemView
    }
}