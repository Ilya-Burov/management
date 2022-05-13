package com.diploma.timemanager

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_task_details.*
import java.util.*

class TaskDetailsActivity : AppCompatActivity() {
    private lateinit var dataManager: DataManager
    private var existingTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        dataManager = DataManager(this)
        val task = intent.getSerializableExtra("assignment")

        if (task is Task) {
            existingTask = task
            etTitle.setText(task.title)
            etDescription.setText(task.description)

            val cal = Calendar.getInstance()
            cal.time = task.deadline
            datePicker.init(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                null
            )
            timePicker.hour = cal.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = cal.get(Calendar.MINUTE)

            addButton.text = getString(R.string.update)
            title = getString(R.string.edit_task)
        } else {
            title = getString(R.string.add_task)
        }

        timePicker.setIs24HourView(true)

        addButton.setOnClickListener { onAddButtonClicked() }
    }

    private fun onAddButtonClicked() {
        val title = etTitle.text.toString()
        val description = etDescription.text.toString()
        val date = getDateFromPickers()
        val immutableExistingTask = existingTask

        if (immutableExistingTask != null) {
            immutableExistingTask.title = title
            immutableExistingTask.description = description
            immutableExistingTask.deadline = date

            dataManager.update(immutableExistingTask)
        } else {
            val task = existingTask ?: Task(null, title, description, date)
            dataManager.add(task)
        }

        finish()
    }

    private fun getDateFromPickers(): Date {
        val cal = Calendar.getInstance()
        cal.set(datePicker.year, datePicker.month, datePicker.dayOfMonth, timePicker.hour, timePicker.minute)
        return cal.time
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
