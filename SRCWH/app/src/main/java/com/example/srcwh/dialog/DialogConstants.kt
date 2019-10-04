package com.example.srcwh.dialog

import android.content.Context
import com.example.srcwh.R

class DialogConstants(contect: Context) {
    val BUTTON_OK = "Okay"
    val BUTTON_CONFIRM = "Confirm"
    val BUTTON_CANCEL = "Cancel"
    val BUTTON_ENABLE = "Enable"

    val ICON_LOADING = R.drawable.ic_radiobox_blank
    val ICON_CHECK = R.drawable.ic_checkbox_marked_circle_outline
    val ICON_LOCATION = R.drawable.ic_map_marker_circle
    val ICON_ERROR_LOCATION = R.drawable.ic_location_error
    val ICON_OVERRIDE = R.drawable.ic_override
    val ICON_ERROR = R.drawable.ic_emoticon_sad_outline
    val ICON_CLOCK = R.drawable.ic_clock_outline

    val TITLE_LOADING = ""
    val TITLE_ATTENDED = "You're in"
    val TITLE_CONFIRM = "You sure?"
    val TITLE_ERROR = "Uh oh…"

    val TEXT_ERROR_POSITION = "You have to be at ADDRESS to attend this lesson."
    val TEXT_ERROR_LOCATION = "“LESSON” is held in REAL_LOCATION, and you are checking in at LOCATION. Are you sure you want to check in?"
    val TEXT_ATTENDED = contect.getString(R.string.dialog_text_attended)
    val TEXT_OVERRIDE = "You have already attended “LESSON” in another location, are you sure you want to switch to LOCATION"
    val TEXT_ERROR_PERMISSION_POSITION = "Please enable location to attend this lesson."
    val TEXT_ERROR_PERMISSION_POSITION_DISABLED = "You have disabled location. Please enable location to attend this lesson."
    val TEXT_ERROR_LESSON = "You can only attend ongoing lessons, and your schedule seems to be empty at the moment."
    val TEXT_ERROR = "Something went wrong.<br/>Please try again soon."
}