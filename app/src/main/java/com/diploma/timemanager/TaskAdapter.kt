package com.diploma.timemanager

import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_task.view.*

class TaskAdapter(
    private val onItemClickListener: (Task) -> Unit,
    private val onMenuItemClickListener: (Task, MenuItem) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    private val tasks = mutableListOf<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, onItemClickListener, onMenuItemClickListener)
    }

    override fun getItemCount() = tasks.size

    fun setTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(task: Task, onItemClickListener: (Task) -> Unit, onMenuItemClickListener: (Task, MenuItem) -> Unit) {
            itemView.text.text = task.toString()
            itemView.setOnClickListener {
                onItemClickListener.invoke(task)
            }

            itemView.setOnLongClickListener { view ->
                val popup = PopupMenu(view.context, view, Gravity.END)
                popup.inflate(R.menu.menu_tasks_context)
                popup.menu.findItem(R.id.menu_complete).isVisible = !task.isCompleted
                popup.setOnMenuItemClickListener { menuItem ->
                    onMenuItemClickListener.invoke(task, menuItem)
                    return@setOnMenuItemClickListener true
                }
                popup.show()

                return@setOnLongClickListener true
            }
        }

    }
}