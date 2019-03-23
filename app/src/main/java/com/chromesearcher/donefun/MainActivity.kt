package com.chromesearcher.donefun

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val tasks: ArrayList<TaskData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addTasks()
        addTasks()
        addTasks()

        recyclerView = findViewById<RecyclerView>(R.id.tasks_recycler_view)


        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = TaskAdapter(this, tasks)



    }

    fun addTasks() {

        val build = R.drawable.ic_baseline_build_24px
        val shopping = R.drawable.ic_shopping_cart_black_24dp
        val wc = R.drawable.ic_wc_black_24dp

        tasks.add(TaskData("wash", R.drawable.ic_baseline_build_24px))
        tasks.add(TaskData("homework", R.drawable.ic_shopping_cart_black_24dp))
        tasks.add(TaskData("dickkicking", R.drawable.ic_wc_black_24dp))



        tasks.add(TaskData("fix stuff", build))
        tasks.add(TaskData("dry the gloryhole", wc))
        tasks.add(TaskData("three naryada wne ocheredy", wc))


        tasks.add(TaskData("ZAWOD", R.drawable.ic_baseline_build_24px))
        tasks.add(TaskData("buy new brother", R.drawable.ic_shopping_cart_black_24dp))
        tasks.add(TaskData("incest", R.drawable.ic_wc_black_24dp))
    }
}
