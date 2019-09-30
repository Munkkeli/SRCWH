package com.example.srcwh.dialog

import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.srcwh.*

enum class DialogInitialState {
    LOADING,
    ERROR,
    POSITION_ERROR,
    POSITION_BLOCK_ERROR,
}

class DialogContainerFragment(
    private val initialState: DialogInitialState,
    private val actionHandler: (action: DialogAction) -> Unit
) : DialogFragment() {
    private lateinit var DC: DialogConstants

    var isFirstLoad = true
    var lastHeight = 0

    var containerView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.dialog_success, null)

        DC = DialogConstants(context!!)

        this.containerView = view
        setState(initialState)

        return view
    }

    fun setState(initialState: DialogInitialState) {
        when (initialState) {
            DialogInitialState.ERROR -> setError()
            DialogInitialState.POSITION_ERROR -> setErrorPositionPermission(false)
            DialogInitialState.POSITION_BLOCK_ERROR -> setErrorPositionPermission(true)
            else -> setLoading()
        }
    }

    fun setLoading() {
        DialogBuilder(this).begin()
            .setViewTemplate(DialogViewTemplate.LOADING)
            .setOnActionHandler(actionHandler)
            .build()
    }

    fun setAttended(location: String?, lesson: ScheduleResponse?) {
        val name = lesson?.name ?: "?"

        DialogBuilder(this).begin()
            .setViewTemplate(DialogViewTemplate.SUCCESS)
            .setTitle(DC.TITLE_ATTENDED)
            .setText(
                DC.TEXT_ATTENDED.replace("LESSON", name).replace(
                    "LOCATION",
                    "<b>${location ?: "?"}</b>"
                )
            )
            .setOnActionHandler(actionHandler)
            .build()
    }

    fun setOverride(location: String?, lesson: ScheduleResponse?) {
        val name = lesson?.name ?: "?"

        DialogBuilder(this).begin()
            .setViewTemplate(DialogViewTemplate.QUESTION)
            .setIcon(DC.ICON_OVERRIDE)
            .setText(
                DC.TEXT_OVERRIDE
                    .replace("LESSON", name)
                    .replace("LOCATION", location ?: "?")
            )
            .setOnActionHandler(actionHandler)
            .build()
    }

    fun setConfirm(location: String?, lesson: ScheduleResponse?) {
        val name = lesson?.name ?: "?"
        val lessonLocationList = lesson?.locationList?.toArray() ?: arrayOf<String>()

        DialogBuilder(this).begin()
            .setViewTemplate(DialogViewTemplate.QUESTION)
            .setLocation(lessonLocationList[0] as String? ?: "?")
            .setText(
                DC.TEXT_ERROR_LOCATION
                    .replace("LESSON", name)
                    .replace("REAL_LOCATION", lessonLocationList.joinToString("or"))
                    .replace("LOCATION", location ?: "?")
            )
            .setOnActionHandler(actionHandler)
            .build()
    }

    fun setErrorPosition(lesson: ScheduleResponse?) {
        val address = "TODO"

        DialogBuilder(this).begin()
            .setViewTemplate(DialogViewTemplate.ERROR)
            .setIcon(DC.ICON_LOCATION)
            .setTitle(DC.TITLE_ERROR)
            .setText(DC.TEXT_ERROR_POSITION.replace("ADDRESS", address))
            .setOnActionHandler(actionHandler)
            .build()
    }

    fun setErrorPositionPermission(isDisabled: Boolean) {
        val builder = DialogBuilder(this).begin()
            .setViewTemplate(DialogViewTemplate.ERROR)
            .setIcon(DC.ICON_ERROR_LOCATION)
            .setTitle(DC.TITLE_ERROR)
            .setText(DC.TEXT_ERROR_PERMISSION_POSITION)
            .setActions(DC.BUTTON_ENABLE, null, DC.BUTTON_CANCEL)

        if (isDisabled) {
            builder
                .setText(DC.TEXT_ERROR_PERMISSION_POSITION_DISABLED)
                .setActions(DC.BUTTON_OK)
                .setDismissible()
        }

        builder
            .setOnActionHandler(actionHandler)
            .build()
    }

    fun setError() {
        DialogBuilder(this).begin().setViewTemplate(DialogViewTemplate.ERROR).build()
    }

    fun close() {
        this.dismiss()
    }
}