package com.chromesearcher.donefun

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.task_item.view.*

public class TaskAdapter (val context: Context, val tasks: ArrayList<TaskData>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.taskNameTextView.text = tasks[position].text
        holder.taskImageView.setImageResource(tasks[position].iconId)
    }

    class TaskViewHolder (itemView: View) : RecyclerView.ViewHolder (itemView){

        val taskNameTextView = itemView.task_name_textview
        val taskImageView = itemView.task_imageView
    }

}