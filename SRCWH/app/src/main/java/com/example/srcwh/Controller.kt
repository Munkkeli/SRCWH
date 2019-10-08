package com.example.srcwh

import android.app.Activity
import android.content.Intent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Controller {
    // Central source of time for easier testing
    val time: ZonedDateTime
        get() = ZonedDateTime.parse("2019-09-17T${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))}+00:00[Europe/Helsinki]")
}