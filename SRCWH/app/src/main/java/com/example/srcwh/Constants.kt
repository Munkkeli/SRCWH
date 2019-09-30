package com.example.srcwh

const val PERMISSIONS_REQUEST_LOCATION = 1

const val BASE_URL = "https://srcwh.xyz"
const val LOGIN_URL = "${BASE_URL}/login"
const val UPDATE_URL = "${BASE_URL}/update"
const val ATTEND_URL = "${BASE_URL}/attend"
const val SCHEDULE_URL = "${BASE_URL}/schedule"

const val AUTH_HEADER = "Authorization"

const val GENERIC_ERROR = "Something went wrong. Please try again later."
const val LOGIN_ERROR = "Invalid credentials. Please try again."
const val GROUP_ERROR = "You must choose a group."

const val DIALOG_OK = "Okay"
const val DIALOG_CONFIRM = "Confirm"
const val DIALOG_CANCEL = "Cancel"
const val DIALOG_LOCATION = "Enable"

const val DIALOG_ICON_LOADING = R.drawable.ic_radiobox_blank
const val DIALOG_ICON_CHECK = R.drawable.ic_checkbox_marked_circle_outline
const val DIALOG_ICON_LOCATION = R.drawable.ic_map_marker_circle
const val DIALOG_ICON_ERROR_LOCATION = R.drawable.ic_map_marker_remove

const val DIALOG_TITLE_ATTENDED = "You're in"
const val DIALOG_TITLE_CONFIRM = "You sure?"
const val DIALOG_TITLE_ERROR = "Uh oh…"

const val DIALOG_TEXT_ERROR_POSITION = "You have to be at POSITION to attend this lesson."
const val DIALOG_TEXT_ERROR_LOCATION = "“LESSON” is held in classroom LOCATION, are you sure you want to check in to another classroom?"
const val DIALOG_TEXT_ATTENDED = "You have checked in to “LESSON” at LOCATION"
const val DIALOG_TEXT_ERROR_PERMISSION_LOCATION = "Please enable location to attend this lesson."
const val DIALOG_TEXT_ERROR_PERMISSION_LOCATION_DISABLED = "You have disabled location. Please enable location to attend this lesson."

const val SHARED_PREFERENCES = "sharedPrefs"
const val DARK_MODE_ON = "false"