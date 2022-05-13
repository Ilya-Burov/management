package com.diploma.timemanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

class DataManager(context: Context) {
    private val db: SQLiteDatabase =
        context.openOrCreateDatabase("TimeManager", Context.MODE_PRIVATE, null)

    init {
        val tasksCreateQuery =
            "CREATE TABLE IF NOT EXISTS `Tasks` (`Id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `Title` TEXT NOT NULL, `Description` INTEGER NOT NULL, `Deadline` INTEGER, `Completed` INT DEFAULT -1)"
        db.execSQL(tasksCreateQuery)
    }

    fun add(task: Task) {
        val query =
            "INSERT INTO Tasks (Title, Description, Deadline) VALUES ('${task.title}', '${task.description}', ${task.deadline.time})"
        db.execSQL(query)
    }

    private fun get(query: String, args: Array<String>?): List<Task> {
        val tasks = mutableListOf<Task>()
        val cursor = db.rawQuery(query, args)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("Id"))
                val title = cursor.getString(cursor.getColumnIndex("Title"))
                val description = cursor.getString(cursor.getColumnIndex("Description"))
                val dateLong = cursor.getLong(cursor.getColumnIndex("Deadline"))
                val completed = cursor.getInt(cursor.getColumnIndex("Completed"))
                val date = Date(dateLong)
                val task = Task(id, title, description, date, completed)
                tasks.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()

        quickSort(tasks, 0, tasks.size - 1)
        return tasks
    }

    private fun quickSort(list: MutableList<Task>, low: Int, high: Int) {
        if (list.isEmpty()) return

        if (low >= high) return

        val middle = low + (high - low) / 2
        val pivot = list[middle]
        var i = low
        var j = high
        while (i <= j) {
            while (list[i] < pivot) {
                i++
            }
            while (list[j] > pivot) {
                j--
            }
            if (i <= j) {
                val temp = list[i]
                list[i] = list[j]
                list[j] = temp
                i++
                j--
            }
        }

        if (low < j) quickSort(list, low, j)
        if (high > i) quickSort(list, i, high)
    }

    fun getTasksForDate(date: LocalDate): List<Task> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay =
            date.plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()

        val query = "SELECT * FROM Tasks WHERE `Deadline` >= $startOfDay AND `Deadline` <= $endOfDay"
        return get(query, null)
    }

    fun getTasksForInterval(start: LocalDate, end: LocalDate): List<Task> {
        val startOfInterval = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfInterval =
            end.plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()

        val query = "SELECT * FROM Tasks WHERE `Deadline` >= $startOfInterval AND `Deadline` <= $endOfInterval"
        return get(query, null)
    }

    fun getTasksForQuery(searchQuery: String): List<Task> {
        val query = "SELECT * FROM Tasks"
        val tasks = get(query, null)
        return linearSearch(tasks, searchQuery)
    }

    private fun linearSearch(tasks: List<Task>, query: String): List<Task> {
        val newTasks = mutableListOf<Task>()
        for (task in tasks) {
            if (task.title.contains(query) || task.description.contains(query)) {
                newTasks.add(task)
            }
        }
        return newTasks
    }

    fun update(task: Task) {
        if (task.id != null) {
            val contentValues = ContentValues()
            contentValues.put("Title", task.title)
            contentValues.put("Description", task.description)
            contentValues.put("Deadline", task.deadline.time)
            contentValues.put("Completed", task.completed)
            val args = arrayOf(task.id.toString())
            db.update("Tasks", contentValues, "Id = ?", args)
        }
    }

    fun delete(task: Task) {
        if (task.id != null) {
            val whereClause = "Id = ?"
            val whereArgs = arrayOf(task.id.toString())
            db.delete("Tasks", whereClause, whereArgs)
        }
    }

    fun complete(task: Task) {
        if (task.id != null) {
            val contentValues = ContentValues()
            contentValues.put("Completed", task.completed)
            val args = arrayOf(task.id.toString())
            db.update("Tasks", contentValues, "Id = ?", args)
        }
    }
}
