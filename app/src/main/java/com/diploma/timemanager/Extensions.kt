package com.diploma.timemanager

import android.view.View
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun LocalDate.millis(): Long {
    val cal = Calendar.getInstance()
    cal.set(year, monthValue - 1, dayOfMonth)
    return cal.timeInMillis
}

fun LocalDate.toDate(): Date {
    return Date.from(this.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
}

fun Date.time(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.UK)
    return formatter.format(this)
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.remove() {
    visibility = View.GONE
}