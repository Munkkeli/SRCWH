package com.example.srcwh

const val PERMISSIONS_REQUEST_LOCATION = 1

const val BASE_URL = "https://srcwh.xyz"
const val LOGIN_URL = "${BASE_URL}/login"
const val UPDATE_URL = "${BASE_URL}/update"
const val ATTEND_URL = "${BASE_URL}/attend"

const val AUTH_HEADER = "Authorization"

const val GENERIC_ERROR = "Something went wrong. Please try again later."
const val LOGIN_ERROR = "Invalid credentials. Please try again."
const val GROUP_ERROR = "You must choose a group."

const val DIALOG_OK = "Okay"
const val DIALOG_CONFIRM = "Confirm"
const val DIALOG_CANCEL = "Cancel"

const val DIALOG_TITLE_ATTENDED = "You're in"
const val DIALOG_TITLE_CONFIRM = "You sure?"
const val DIALOG_TITLE_ERROR = "Uh oh…"

const val DIALOG_TEXT_ERROR_LOCATION = "You have to be at LOCATION to attend this lesson."