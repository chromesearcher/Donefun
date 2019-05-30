package com.chromesearcher.donefun

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.board_item.view.*

class BoardAdapter (private val context: Context, private val boards: ArrayList<Board>)
    : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    private lateinit var onItemClickListener: View.OnClickListener
    private lateinit var onItemLongClickListener: View.OnLongClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.board_item, parent, false)
        return BoardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return boards.size
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.boardNameTextView.text = boards[position].name
        holder.actorNameTextView.text = boards[position].actor

        val parent = holder.boardNameTextView.parent as RelativeLayout
        parent.background = ColorDrawable(Color.WHITE)
    }

    fun setOnItemClickListener(itemClickListener: View.OnClickListener) {
        onItemClickListener = itemClickListener
    }

    fun setOnItemLongClickListener(itemLongClickListener: View.OnLongClickListener) {
        onItemLongClickListener = itemLongClickListener
    }

    inner class BoardViewHolder (itemView: View) : RecyclerView.ViewHolder (itemView){

        init {
            itemView.tag = this
            itemView.setOnClickListener(onItemClickListener)
            itemView.setOnLongClickListener(onItemLongClickListener)
        }

        val boardNameTextView: TextView = itemView.textView_boardname
        val actorNameTextView: TextView = itemView.textView_actorname

    }

}