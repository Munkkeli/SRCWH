package com.example.srcwh.dialog

import android.content.Context
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager

enum class DialogAction {
    PRIMARY,
    SECONDARY,
    CANCEL
}

class DialogHandler {
    var context: Context
    private var fragmentManager: FragmentManager
    private var dialogContainerFragment: DialogContainerFragment? = null
    private lateinit var container: FrameLayout

    var currentActionHandler: ((action: DialogAction) -> Unit)? = null

    constructor(context: Context, fragmentManager: FragmentManager) {
        this.context = context
        this.fragmentManager = fragmentManager

        // builder = createBlankDialogBuilder()
        // dialog = builder.create()
    }

    val isOpen
        get() = dialogContainerFragment != null && dialogContainerFragment!!.isVisible

    private fun dialogActionHandler(action: DialogAction) {
        if (currentActionHandler != null) currentActionHandler!!(action)
    }

    fun open(firstFragment: String = "loading", locationDisabled: Boolean = false, callback: (action: DialogAction) -> Unit = {}) {
        currentActionHandler = callback

        val transaction = fragmentManager.beginTransaction()
        val prev = fragmentManager.findFragmentByTag("dialog")

        if (prev != null) transaction.remove(prev)

        // transaction.addToBackStack(null)

        dialogContainerFragment = DialogContainerFragment(
            firstFragment,
            locationDisabled
        ) { action -> dialogActionHandler(action) }

        dialogContainerFragment!!.show(transaction, "dialog")
    }

    fun close() {
        dialogContainerFragment!!.close()
    }

    fun setCheckIn() {
        dialogContainerFragment!!.setCheckIn()
    }

    fun setConfirm() {
        dialogContainerFragment!!.setConfirm()
    }

    fun setErrorLocation() {
        dialogContainerFragment!!.setErrorLocation()
    }

    fun setErrorPermissionLocation(locationDisabled: Boolean, callback: (action: DialogAction) -> Unit) {
        currentActionHandler = callback
        dialogContainerFragment!!.setErrorPermissionLocation(locationDisabled)
    }
}