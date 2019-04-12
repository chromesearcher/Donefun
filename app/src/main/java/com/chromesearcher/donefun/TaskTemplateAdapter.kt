package com.chromesearcher.donefun

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.task_template.view.*

class TaskTemplateAdapter (private val context: Context, private val templates: ArrayList<TaskTemplate>) : RecyclerView.Adapter<TaskTemplateAdapter.TaskTemplateViewHolder>() {

    private lateinit var onItemClickListener: View.OnClickListener
    private lateinit var onItemLongClickListener: View.OnLongClickListener


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskTemplateViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.task_template, parent, false)

//        view.setOnClickListener {
//            val itemPosition =
//        }
        return TaskTemplateViewHolder(view)
    }

    override fun getItemCount(): Int {
        return templates.size
    }

    override fun onBindViewHolder(holder: TaskTemplateViewHolder, position: Int) {

        val template: SelectableTaskTemplate = templates[position] as SelectableTaskTemplate

        holder.taskTemplateNameTextView.text = template.text
//        holder.taskImageView.setImageResource(templates[position].iconId)

        val parent = holder.taskTemplateNameTextView.parent as ConstraintLayout

        if (template.selected) {
            parent.background = ColorDrawable(Color.parseColor("#80deea"))
        } else {
            parent.background = ColorDrawable(Color.WHITE)
        }
    }

    fun setOnItemClickListener(itemClickListener: View.OnClickListener) {
        onItemClickListener = itemClickListener
    }

    fun setOnItemLongClickListener(itemLongClickListener: View.OnLongClickListener) {
        onItemLongClickListener = itemLongClickListener
    }

    inner class TaskTemplateViewHolder (itemView: View) : RecyclerView.ViewHolder (itemView){

        init {
            itemView.tag = this
            itemView.setOnClickListener(onItemClickListener)
            itemView.setOnLongClickListener(onItemLongClickListener)
        }


        val taskTemplateNameTextView = itemView.template_name_tv
//        val taskImageView = itemView.task_imageView

    }

}