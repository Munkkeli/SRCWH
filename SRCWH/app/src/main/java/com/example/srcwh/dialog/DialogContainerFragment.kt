package com.example.srcwh.dialog

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.srcwh.*

class DialogContainerFragment(private val firstFragment: String, private val locationDisabled: Boolean = false, private val actionHandler: (action: DialogAction) -> Unit) : DialogFragment() {
    var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.dialog_success, null)

        when(firstFragment) {
            "loading" -> setLoading()
            "location_permission" -> setErrorPermissionLocation(locationDisabled)
        }

        return view
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
    }

    fun setCheckIn() {
        changeFragment(
            DialogContentFragment(
                DIALOG_ICON_CHECK,
                DIALOG_TITLE_ATTENDED,
                DIALOG_TEXT_ATTENDED,
                null
            )
        )
    }

    fun setConfirm() {
        changeFragment(
            DialogContentFragment(
                null,
                DIALOG_TITLE_CONFIRM,
                DIALOG_TEXT_ERROR_LOCATION,
                "C223"
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
    }

    fun setErrorLocation() {
        changeFragment(
            DialogContentFragment(
                DIALOG_ICON_LOCATION,
                DIALOG_TITLE_ERROR,
                DIALOG_TEXT_ERROR_POSITION.replace("POSITION", "Leiritie 1"),
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
    }

    fun setErrorPermissionLocation(isDisabled: Boolean) {
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
        } else {
            changeActionFragment(
                DialogActionFragment(
                    DIALOG_OK,
                    null,
                    null,
                    actionHandler
                )
            )
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