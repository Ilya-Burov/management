package com.diploma.timemanager

import java.io.Serializable
import java.util.*

class Task(
    var id: Int?,
    var title: String,
    var description: String,
    var deadline: Date,
    var completed: Int = -1
) : Comparable<Task>, Serializable {

    val isCompleted: Boolean
        get() = completed != -1

    override fun compareTo(other: Task): Int {
        return deadline.compareTo(other.deadline)
    }

    private fun daysLeft(): Int {
        val difference = (deadline.time - Date().time) / (1000 * 60 * 60 * 24)
        return difference.toInt()
    }

    override fun toString(): String {
        if (isCompleted) {
            return "${deadline.time()}: $title (затраченное время: ${format(completed)})"
        }

        return "${deadline.time()}: $title"
    }

    private fun format(minutes: Int): String {
        return pad(minutes / 60) + ":" + pad(minutes % 60)
    }

    private fun pad(x: Int): String {
        return if (x < 10) "0$x" else x.toString() + ""
    }
}