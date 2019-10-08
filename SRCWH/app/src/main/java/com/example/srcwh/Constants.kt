package com.example.srcwh

const val PERMISSIONS_REQUEST_LOCATION = 1
const val QR_ACTIVITY_REQUEST_CODE = 3

const val BASE_URL = "https://srcwh.xyz"
const val LOGIN_URL = "${BASE_URL}/login"
const val UPDATE_URL = "${BASE_URL}/update"
const val ATTEND_URL = "${BASE_URL}/attend"
const val SCHEDULE_URL = "${BASE_URL}/schedule"
const val CHECK_URL = "${BASE_URL}/check"

const val AUTH_HEADER = "Authorization"

const val QR_REGEX = "$BASE_URL/qr/[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"

const val GENERIC_ERROR = "Something went wrong. Please try again later."
const val LOGIN_ERROR = "Invalid credentials. Please try again."

const val NOTIF_CHANNEL_ID = "srcwh_notif_id"
const val NOTIF_CHANNEL_NAME = "srcwh"

const val GROUP_ERROR = "You must choose a group."

