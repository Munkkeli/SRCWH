package com.example.srcwh.dialog

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.srcwh.ScheduleResponse

enum class DialogAction {
    PRIMARY,
    SECONDARY,
    CANCEL
}

typealias ActionCallback = ((action: DialogAction) -> Unit)?

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
        Log.d("DIALOG", "dialogActionHandler $action currentActionHandler $currentActionHandler")

        if (currentActionHandler != null) {
            currentActionHandler!!(action)
        } else if (action == DialogAction.PRIMARY || action == DialogAction.CANCEL) {
            close()
        }
    }

    fun open(initialState: DialogInitialState = DialogInitialState.LOADING, callback: ActionCallback? = null) {
        currentActionHandler = callback

        val transaction = fragmentManager.beginTransaction()
        val prev = fragmentManager.findFragmentByTag("dialog")

        if (prev != null) transaction.remove(prev)

        // transaction.addToBackStack(null)

        if (isOpen) {
            dialogContainerFragment!!.setState(initialState)
        } else {
            dialogContainerFragment = DialogContainerFragment(initialState) { action -> dialogActionHandler(action) }
            dialogContainerFragment!!.show(transaction, "dialog")
        }
    }

    fun close() {
        dialogContainerFragment!!.close()
    }

    fun setAttended(location: String?, lesson: ScheduleResponse?, callback: ActionCallback? = null) {
        if (callback != null) currentActionHandler = callback
        dialogContainerFragment!!.setAttended(location, lesson)
    }

    fun setOverride(location: String?, lesson: ScheduleResponse?, callback: ActionCallback? = null) {
        if (callback != null) currentActionHandler = callback
        dialogContainerFragment!!.setOverride(location, lesson)
    }

    fun setConfirm(location: String?, lesson: ScheduleResponse?, callback: ActionCallback? = null) {
        if (callback != null) currentActionHandler = callback
        dialogContainerFragment!!.setConfirm(location, lesson)
    }

    fun setErrorLesson() {
        dialogContainerFragment!!.setErrorLesson()
    }

    fun setErrorPosition(lesson: ScheduleResponse?) {
        dialogContainerFragment!!.setErrorPosition(lesson)
    }

    fun setError() {
        dialogContainerFragment!!.setError()
    }

    fun setErrorPositionPermission(locationDisabled: Boolean, callback: (action: DialogAction) -> Unit) {
        currentActionHandler = callback
        dialogContainerFragment!!.setErrorPositionPermission(locationDisabled)
    }
}