package com.chromesearcher.donefun

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.task_item.view.*
import kotlinx.android.synthetic.main.task_item.view.task_imageView
import kotlinx.android.synthetic.main.task_item.view.task_name_textview
import kotlinx.android.synthetic.main.task_item2.view.*

class TaskAdapter (private val context: Context, private val tasks: ArrayList<Task>)
    : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {


    private lateinit var onItemClickListener: View.OnClickListener
    private lateinit var onItemLongClickListener: View.OnLongClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.task_item2, parent, false)

        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.taskNameTextView.text = tasks[position].template.text

        if (tasks[position].status == "IN PROGRESS") {
            holder.statusImageView.visibility = View.VISIBLE
        } else {
            holder.statusImageView.visibility = View.INVISIBLE
        }

        val parent = holder.taskNameTextView.parent as RelativeLayout
        parent.background = ColorDrawable(Color.WHITE)
    }

    fun setOnItemClickListener(itemClickListener: View.OnClickListener) {
        onItemClickListener = itemClickListener
    }

    fun setOnItemLongClickListener(itemLongClickListener: View.OnLongClickListener) {
        onItemLongClickListener = itemLongClickListener
    }

    inner class TaskViewHolder (itemView: View) : RecyclerView.ViewHolder (itemView){

        init {
            itemView.tag = this
            itemView.setOnClickListener(onItemClickListener)
            itemView.setOnLongClickListener(onItemLongClickListener)
        }

        val taskNameTextView = itemView.task_name_textview
        val statusImageView = itemView.status_imageView
    }

}