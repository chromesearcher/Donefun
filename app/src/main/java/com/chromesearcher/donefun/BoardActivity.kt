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
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric



class BoardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton

    private val templates: ArrayList<TaskTemplate> = ArrayList()
    private val tasks: ArrayList<Task> = ArrayList()

    private val tasksCollection: String = "taskInstances"
    private val templatesCollection: String = "taskTypes"

    private val db = FirebaseFirestore.getInstance()
    private val source = Source.DEFAULT

    private val TAG: String = "myLogs"
    private val BOARD: String = "main"


    private val onItemClickListener: View.OnClickListener = View.OnClickListener{

        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition
        val item: Task = tasks[pos]

        val id = item.id
        val status = item.status
        val template = item.template

        val newStatus = if (status.equals("IN PROGRESS")) "DONE" else "IN PROGRESS"


        Toast.makeText(applicationContext, "FUK U: " + item.template.text, Toast.LENGTH_SHORT).show()

        // add DB interaction (update status)
        db.collection(tasksCollection).document(id)
                .update("status", newStatus)
                .addOnSuccessListener {
                    val newTask = Task(newStatus, template, id)
                    tasks.set(pos, newTask)

                    Log.d(TAG, "DocumentSnapshot (task) successfully written|updated!")

                    // TODO: make it safe
                    recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing|updating task", e) }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_board)

        fabAddTask = findViewById(R.id.fabAddTask)

        fabAddTask.setOnClickListener {
            Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()

            // invoke LibActivity

            var intent = Intent(this, LibActivity::class.java)
            intent.putExtra("mode", "ADD")
            intent.putExtra("board", BOARD)
            startActivity(intent)
        }

        db.collection(templatesCollection)
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs) {
                        val iconId = (doc.data["iconId"] as String).toInt()
                        val name = doc.data["name"] as String
                        val id = doc.id

                        var icon: Int = 0

                        when (iconId) {
                            0 -> icon = R.drawable.ic_baseline_build_24px
                            1 -> icon = R.drawable.ic_shopping_cart_black_24dp
                            2 -> icon = R.drawable.ic_wc_black_24dp
                        }

                        templates.add(TaskTemplate(icon, name, id))
                    }

                    db.collection(tasksCollection)
                            .whereEqualTo("board", BOARD)
                            .get()
                            .addOnSuccessListener { docs ->
                                for (doc in docs) {
                                    Log.d(TAG, "${doc.id} => ${doc.data}")

                                    val status = doc.data["status"] as String
                                    val typeId = doc.data["typeId"] as String
                                    val id = doc.id

                                    for (t in templates) {
                                        if (t.id.equals(typeId)) {
                                            tasks.add(Task(status, t, id))
                                        }
                                    }

                                    recyclerView = findViewById(R.id.tasks_recycler_view)
                                    recyclerView.layoutManager = LinearLayoutManager(this)

                                    val myAdapter = TaskAdapter(this, tasks)
                                    recyclerView.adapter = myAdapter

                                    myAdapter.setOnItemClickListener(onItemClickListener)
                                }
                            }
                            .addOnFailureListener { exc ->
                                Log.w(TAG, "Error getting taskInstances: ", exc)
                            }
                }
                .addOnFailureListener {exc ->
                    Log.w(TAG, "Error getting taskTypes: ", exc)
                }
    }
}