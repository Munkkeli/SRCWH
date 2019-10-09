package com.example.srcwh

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.example.srcwh.dialog.DialogAction
import com.example.srcwh.dialog.DialogHandler
import com.example.srcwh.dialog.DialogInitialState

/*
 * Main class to handle check in, will open a dialog and guide the user through any necessary steps
 */
class AttendHandler(private val context: Context, fragmentManager: FragmentManager) {
    private val locationHandler: LocationHandler = LocationHandler(context)
    private val dialogHandler: DialogHandler = DialogHandler(context, fragmentManager)

    val isOpen
        get() = dialogHandler.isOpen

    fun attend(
        slabId: String,
        confirmUpdate: Boolean = false,
        confirmOverride: Boolean = false,
        comingFromExplain: Boolean = false
    ) {
        val user = DatabaseObj.getUserData()

        locationHandler.getCoordinates(comingFromExplain) { error, coordinates ->
            if (error != null || coordinates == null) {
                // Did not get the location
                when (error) {
                    LocationError.DENIED -> dialogHandler.open(DialogInitialState.POSITION_ERROR)
                    LocationError.BLOCKED -> dialogHandler.open(DialogInitialState.POSITION_BLOCK_ERROR)
                    LocationError.EXPLAIN -> dialogHandler.open(DialogInitialState.POSITION_ERROR) { action ->
                        dialogHandler.close()
                        if (action == DialogAction.PRIMARY) {
                            this.attend(slabId, confirmUpdate, confirmOverride, true)
                        } else {
                            dialogHandler.close()
                        }
                    }
                    else -> dialogHandler.open(DialogInitialState.ERROR)
                }
            } else {
                // Did get a location
                dialogHandler.open()

                val networkHandler = NetworkHandler()
                networkHandler.postAttend(
                    user!!.token!!,
                    slabId,
                    coordinates,
                    confirmUpdate,
                    confirmOverride
                ) { postError, location, lesson ->
                    if (postError != null) {
                        when (postError) {
                            AttendError.LESSON -> dialogHandler.setErrorLesson()
                            AttendError.LOCATION -> dialogHandler.setConfirm(
                                location,
                                lesson
                            ) { action ->
                                if (action == DialogAction.PRIMARY) {
                                    this.attend(
                                        slabId,
                                        confirmUpdate = confirmUpdate,
                                        confirmOverride = true
                                    )
                                } else {
                                    dialogHandler.close()
                                }
                            }
                            AttendError.POSITION -> dialogHandler.setErrorPosition(lesson)
                            AttendError.UPDATE -> dialogHandler.setOverride(
                                location,
                                lesson
                            ) { action ->
                                if (action == DialogAction.PRIMARY) {
                                    this.attend(
                                        slabId,
                                        confirmUpdate = true,
                                        confirmOverride = confirmOverride
                                    )
                                } else {
                                    dialogHandler.close()
                                }
                            }
                            else -> dialogHandler.setError()
                        }
                    } else {
                        // Successfully attended
                        dialogHandler.setAttended(location, lesson) {
                            (context as MainActivity).updateSchedule()
                            dialogHandler.close()
                        }
                    }
                }
            }
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Pass on to location handler
        locationHandler.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}