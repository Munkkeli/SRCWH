package com.example.srcwh.dialog

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.srcwh.*

enum class DialogViewState {
    LOADING,
    POSITION_ERROR,
    POSITION_BLOCK_ERROR,
    ATTENDED,
    LOCATION_CONFIRM
}

class DialogContainerFragment(private val initialState: DialogViewState, private val actionHandler: (action: DialogAction) -> Unit) : DialogFragment() {
    var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.dialog_success, null)

        setState(initialState)

        return view
    }

    fun setState(initialState: DialogViewState) {
        when(initialState) {
            DialogViewState.POSITION_ERROR -> setErrorPositionPermission(false)
            DialogViewState.POSITION_BLOCK_ERROR -> setErrorPositionPermission(true)
            else -> setLoading()
        }
    }

    fun setLoading() {
        changeFragment(
            DialogContentFragment(
                DIALOG_ICON_LOADING,
                "…",
                "",
                null
            )
        )

        changeActionFragment(
            DialogActionFragment(
                null,
                null,
                null,
                actionHandler
            )
        )

        isCancelable = false
    }

    fun setAttended(location: String?, lesson: ScheduleResponse?) {
        val name = lesson?.name ?: "?"

        changeFragment(
            DialogContentFragment(
                DIALOG_ICON_CHECK,
                DIALOG_TITLE_ATTENDED,
                DIALOG_TEXT_ATTENDED.replace("LESSON", name).replace("LOCATION", location ?: "?"),
                null
            )
        )

        changeActionFragment(
            DialogActionFragment(
                DIALOG_OK,
                null,
                null,
                actionHandler
            )
        )

        isCancelable = true
    }

    fun setOverride(location: String?, lesson: ScheduleResponse?) {
        val name = lesson?.name ?: "?"

        changeFragment(
            DialogContentFragment(
                DIALOG_ICON_OVERRIDE,
                DIALOG_TITLE_CONFIRM,
                DIALOG_TEXT_OVERRIDE.replace("LESSON", name).replace("LOCATION", location ?: "?"),
                null
            )
        )

        changeActionFragment(
            DialogActionFragment(
                DIALOG_CONFIRM,
                null,
                DIALOG_CANCEL,
                actionHandler
            )
        )

        isCancelable = false
    }

    fun setConfirm(location: String?, lesson: ScheduleResponse?) {
        val name = lesson?.name ?: "?"
        val lessonLocationList = lesson?.locationList?.toArray() ?: arrayOf<String>()

        changeFragment(
            DialogContentFragment(
                null,
                DIALOG_TITLE_CONFIRM,
                DIALOG_TEXT_ERROR_LOCATION.replace("LESSON", name).replace("LOCATION", lessonLocationList.joinToString("or")),
                location ?: "?"
            )
        )

        changeActionFragment(
            DialogActionFragment(
                DIALOG_CONFIRM,
                null,
                DIALOG_CANCEL,
                actionHandler
            )
        )

        isCancelable = false
    }

    fun setErrorPosition(lesson: ScheduleResponse?) {
        val address = "TODO"

        changeFragment(
            DialogContentFragment(
                DIALOG_ICON_LOCATION,
                DIALOG_TITLE_ERROR,
                DIALOG_TEXT_ERROR_POSITION.replace("ADDRESS", address),
                null
            )
        )

        changeActionFragment(
            DialogActionFragment(
                DIALOG_OK,
                null,
                null,
                actionHandler
            )
        )

        isCancelable = false
    }

    fun setErrorPositionPermission(isDisabled: Boolean) {
        var text = DIALOG_TEXT_ERROR_PERMISSION_LOCATION
        if (isDisabled) text = DIALOG_TEXT_ERROR_PERMISSION_LOCATION_DISABLED

        changeFragment(
            DialogContentFragment(
                DIALOG_ICON_ERROR_LOCATION,
                DIALOG_TITLE_ERROR,
                text,
                null
            )
        )

        if (!isDisabled) {
            changeActionFragment(
                DialogActionFragment(
                    DIALOG_LOCATION,
                    null,
                    DIALOG_CANCEL,
                    actionHandler
                )
            )

            isCancelable = false
        } else {
            changeActionFragment(
                DialogActionFragment(
                    DIALOG_OK,
                    null,
                    null,
                    actionHandler
                )
            )

            isCancelable = true
        }
    }

    fun close() {
        this.dismiss()
    }

    private fun changeFragment(fragment: Fragment, inAnim: Int = 0, outAnim: Int = 0) {
        if (isAdded && !childFragmentManager.isDestroyed) {
            val transaction = childFragmentManager.beginTransaction()

            Log.d("DIALOG", "Replacing fragment with ${fragment.toString()}")

            if (!firstLoad) {
                transaction.setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
            } else {
                firstLoad = false
            }

            transaction.replace(R.id.dialog_fragment_container, fragment)
            // transaction.addToBackStack(null)
            transaction.commit()

            childFragmentManager.executePendingTransactions()
        }
    }

    private fun changeActionFragment(fragment: Fragment, inAnim: Int = 0, outAnim: Int = 0) {
        if (isAdded && !childFragmentManager.isDestroyed) {
            val transaction = childFragmentManager.beginTransaction()

            transaction.replace(R.id.dialog_action_container, fragment)
            // transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}