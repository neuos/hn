package eu.neuhuber.hn.ui.util

import java.text.DateFormat
import java.time.Instant
import java.util.*

fun Instant.toLocalString(): String = DateFormat
    .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    .format(Date.from(this))