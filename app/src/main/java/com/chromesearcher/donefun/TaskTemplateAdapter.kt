package com.chromesearcher.donefun

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.task_template.view.*

public class TaskTemplateAdapter (val context: Context, val templates: ArrayList<TaskTemplate>) : RecyclerView.Adapter<TaskTemplateAdapter.TaskTemplateViewHolder>() {


    private lateinit var mOnItemClickListener: View.OnClickListener


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
        holder.taskTemplateNameTextView.text = templates[position].text

//        holder.taskImageView.setImageResource(templates[position].iconId)
    }

    public fun setOnItemClickListener(itemClickListener: View.OnClickListener) {
        mOnItemClickListener = itemClickListener
    }

    inner class TaskTemplateViewHolder (itemView: View) : RecyclerView.ViewHolder (itemView){

        init {
            itemView.tag = this
            itemView.setOnClickListener(mOnItemClickListener)
        }


        val taskTemplateNameTextView = itemView.template_name_tv
//        val taskImageView = itemView.task_imageView

    }

}