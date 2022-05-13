package com.diploma.timemanager

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var dataManager: DataManager
    private lateinit var adapter: TaskAdapter

    private var toast: Toast? = null
    private var selectedDate = LocalDate.now()

    private var searchItem: MenuItem? = null
    private var searchView: SearchView? = null

    private var startDate = LocalDate.now().minusDays(30)
    private var endDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataManager = DataManager(this)
        title = getString(R.string.today_tasks)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_tasks -> {
                    showTasks()
                    selectedDate = LocalDate.now()
                    refreshTasks()
                    true
                }
                R.id.menu_calendar -> {
                    showCalendar()
                    calendarView.date = selectedDate.millis()

                    refreshTasks()
                    true
                }
                R.id.menu_statistics -> {
                    showStatistics()
                    initStatistics()
                    true
                }
                else -> false
            }
        }

        adapter = TaskAdapter(::onTaskClicked, ::onTaskMenuItemClicked)
        tasksRecyclerView.adapter = adapter
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksRecyclerView.addItemDecoration(ListDivider(this))

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            refreshTasks()
        }

        addTaskButton.setOnClickListener {
            val intent = Intent(this, TaskDetailsActivity::class.java)
            startActivity(intent)
        }

        showTasks()
        refreshTasks()
    }

    private fun showTasks() {
        title = getString(R.string.today_tasks)
        tasksRecyclerView.show()
        calendarView.remove()
        divider.remove()
        statisticsTextView.remove()
        startTextView.remove()
        endTextView.remove()
        graph.remove()
        addTaskButton.show()

        updateSearchView(true)
    }

    private fun showCalendar() {
        title = getString(R.string.calendar)
        tasksRecyclerView.show()
        calendarView.show()
        divider.show()
        statisticsTextView.remove()
        startTextView.remove()
        endTextView.remove()
        graph.remove()
        addTaskButton.show()

        updateSearchView(false)
    }

    private fun showStatistics() {
        title = getString(R.string.statistics)
        tasksRecyclerView.remove()
        calendarView.remove()
        divider.remove()
        statisticsTextView.show()
        startTextView.show()
        endTextView.show()
        graph.show()
        addTaskButton.hide()

        updateSearchView(false)
    }

    private fun initStatistics() {
        startDate = LocalDate.now().minusDays(30)
        endDate = LocalDate.now()

        startTextView.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    startDate = LocalDate.of(year, month + 1, dayOfMonth)
                    updateStatistics()
                },
                startDate.year,
                startDate.month.value - 1,
                startDate.dayOfMonth
            )
            datePickerDialog.show()
        }
        endTextView.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    endDate = LocalDate.of(year, month + 1, dayOfMonth)
                    updateStatistics()
                },
                endDate.year,
                endDate.month.value - 1,
                endDate.dayOfMonth
            )
            datePickerDialog.show()
        }

        updateStatistics()
    }

    private fun updateStatistics() {
        val tasks = dataManager.getTasksForInterval(startDate, endDate)

        startTextView.text = getString(R.string.statistics_start, startDate)
        endTextView.text = getString(R.string.statistics_end, endDate)

        var minutes = 0
        for (task in tasks) {
            minutes += if (task.completed == -1) 0 else task.completed
        }
        statisticsTextView.text =
            getString(
                R.string.statistics_text,
                startDate,
                endDate,
                tasks.size,
                tasks.filter { it.isCompleted }.size,
                minutes / 60,
                minutes % 60
            )

        graph.title = getString(R.string.statistics_title)
        graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    val mDateFormat = SimpleDateFormat("dd.MM")
                    val mCalendar = Calendar.getInstance()
                    mCalendar.timeInMillis = value.toLong()
                    mDateFormat.format(mCalendar.timeInMillis)
                } else {
                    "${(value * 100).toInt()}%"
                }

            }
        }
        graph.gridLabelRenderer.setHorizontalLabelsAngle(90)

        graph.viewport.setMaxY(1.0)
        graph.viewport.isYAxisBoundsManual = true

        graph.viewport.setMinX(startDate.toDate().time.toDouble())
        graph.viewport.setMaxX(endDate.toDate().time.toDouble())
        graph.viewport.isXAxisBoundsManual = true

        val points = mutableListOf<DataPoint>()
        var date = startDate
        while (date.isBefore(endDate.plusDays(1))) {
            val tasksForDate = dataManager.getTasksForDate(date)
            val total = tasksForDate.size.toDouble()
            val completed = tasksForDate.count { it.isCompleted }
            val point = if (total == 0.0) 0.0 else completed / total

            points.add(DataPoint(date.toDate(), point))

            date = date.plusDays(1)
        }
        Log.d("<3", "points $points")
        val series = LineGraphSeries(points.toTypedArray())
        graph.addSeries(series)
    }

    private fun onTaskClicked(task: Task) {
        toast?.cancel()
        toast = Toast.makeText(this, task.description, Toast.LENGTH_LONG)
        toast!!.show()
    }

    private fun onTaskMenuItemClicked(task: Task, menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.menu_edit -> {
                val intent = Intent(this, TaskDetailsActivity::class.java)
                intent.putExtra("assignment", task)
                startActivity(intent)
            }
            R.id.menu_delete -> {
                dataManager.delete(task)
                refreshTasks()
            }
            R.id.menu_complete -> {
                showPickerDialog(task)
            }
        }
    }

    private fun showPickerDialog(task: Task) {
        val view = this.layoutInflater.inflate(R.layout.dialog_picker, null)

        val hourPicker = view.findViewById<NumberPicker>(R.id.hours)
        hourPicker.minValue = 0
        hourPicker.maxValue = 99

        val minutesPicker = view.findViewById<NumberPicker>(R.id.minutes)
        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59

        AlertDialog.Builder(this)
            .setView(view)
            .setTitle(getString(R.string.picker_dialog_title))
            .setPositiveButton(R.string.ok) { _, _ ->
                task.completed = hourPicker.value * 60 + minutesPicker.value
                dataManager.complete(task)
                refreshTasks()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun refreshTasks() {
        val tasks = dataManager.getTasksForDate(selectedDate)
        adapter.setTasks(tasks)
    }

    override fun onResume() {
        super.onResume()
        refreshTasks()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        searchItem = menu.findItem(R.id.action_search)
        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                onSearchClosed()
                return true
            }
        })

        searchView = searchItem?.actionView as SearchView
        searchView?.maxWidth = Int.MAX_VALUE
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                onSearchClicked(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean = true
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun onSearchClicked(query: String) {
        val tasks = dataManager.getTasksForQuery(query)
        adapter.setTasks(tasks)
    }

    private fun onSearchClosed() {
        refreshTasks()
    }

    @SuppressLint("RestrictedApi")
    private fun updateSearchView(isVisible: Boolean) {
        if (!isVisible) {
            supportActionBar?.collapseActionView()
        }
        searchItem?.isVisible = isVisible
    }

    override fun onBackPressed() {
        if (searchView?.isIconified == false) {
            searchView?.isIconified = true
        } else {
            super.onBackPressed()
        }
    }
}
