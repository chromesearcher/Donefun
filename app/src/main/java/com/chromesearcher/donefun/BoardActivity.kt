package com.chromesearcher.donefun

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class BoardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton

    private val tasks: ArrayList<Task> = ArrayList()

    private val TAG: String = "myLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        recyclerView = findViewById(R.id.tasks_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TaskAdapter(this, tasks)

        fabAddTask = findViewById(R.id.fabAddTask)

        fabAddTask.setOnClickListener {
            Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()

            // TODO: invoke LibActivity

            var intent = Intent(this, LibActivity::class.java)
            intent.putExtra("mode", "ADD")
            startActivity(intent)
        }
    }
}
