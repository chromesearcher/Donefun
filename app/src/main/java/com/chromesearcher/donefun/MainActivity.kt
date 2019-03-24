package com.chromesearcher.donefun

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val tasks: ArrayList<Task> = ArrayList()
    private val types: ArrayList<TaskType> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addTypes()

        addTasks()
        addTasks()
        addTasks()

        recyclerView = findViewById(R.id.tasks_recycler_view)


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TaskAdapter(this, tasks)
    }

    private fun addTypes() {

        val build = R.drawable.ic_baseline_build_24px
        val shopping = R.drawable.ic_shopping_cart_black_24dp
        val wc = R.drawable.ic_wc_black_24dp

        types.add(TaskType("wash", R.drawable.ic_baseline_build_24px))
        types.add(TaskType("homework", R.drawable.ic_shopping_cart_black_24dp))
        types.add(TaskType("dickkicking", R.drawable.ic_wc_black_24dp))



        types.add(TaskType("fix stuff", build))
        types.add(TaskType("dry the gloryhole", wc))
        types.add(TaskType("three naryada wne ocheredy", wc))


        types.add(TaskType("ZAWOD", R.drawable.ic_baseline_build_24px))
        types.add(TaskType("buy new brother", shopping))
        types.add(TaskType("incest", R.drawable.ic_wc_black_24dp))
    }

    private fun addTasks() {


        tasks.add(Task("IN PROGRESS", types[0]))
        tasks.add(Task("IN PROGRESS", types[1]))
        tasks.add(Task("IN PROGRESS", types[2]))
        tasks.add(Task("IN PROGRESS", types[3]))
        tasks.add(Task("IN PROGRESS", types[4]))
        tasks.add(Task("IN PROGRESS", types[5]))
        tasks.add(Task("IN PROGRESS", types[6]))
        tasks.add(Task("IN PROGRESS", types[7]))
        tasks.add(Task("IN PROGRESS", types[8]))
        tasks.add(Task("DONE", types[0]))

    }
}
