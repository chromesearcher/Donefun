package com.chromesearcher.donefun

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.task_item.view.*

class TaskAdapter (val context: Context, val tasks: ArrayList<Task>)
    : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {


    private lateinit var onItemClickListener: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.taskNameTextView.text = tasks[position].template.text
        holder.taskImageView.setImageResource(tasks[position].template.iconId)
        holder.statusTextView.text = tasks[position].status
    }

    fun setOnItemClickListener(itemClickListener: View.OnClickListener) {
        onItemClickListener = itemClickListener
    }

    inner class TaskViewHolder (itemView: View) : RecyclerView.ViewHolder (itemView){

        init {
            itemView.tag = this
            itemView.setOnClickListener(onItemClickListener)
        }

        val taskNameTextView = itemView.task_name_textview
        val taskImageView = itemView.task_imageView
        val statusTextView = itemView.status_textview
    }

}