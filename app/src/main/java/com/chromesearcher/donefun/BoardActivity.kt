package com.chromesearcher.donefun

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle


class BoardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var toolbar: Toolbar

    private lateinit var board: String

    private val templates: ArrayList<TaskTemplate> = ArrayList()
    private val tasks: ArrayList<Task> = ArrayList()

    private val tasksCollection: String = "taskInstances"
    private val templatesCollection: String = "taskTypes"

    private val db = FirebaseFirestore.getInstance()
    private val source = Source.DEFAULT

    private val TAG: String = "myLogs"
    private val DEFAULT_BOARD: String = "main"


    private val onItemClickListener: View.OnClickListener = View.OnClickListener{

        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition
        val item: Task = tasks[pos]

        val id = item.id
        val status = item.status
        val template = item.template

        val newStatus = if (status == "IN PROGRESS") "DONE" else "IN PROGRESS"


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


    // long tap allows to delete chosen task from current board (and from DB)
    private val onItemLongClickListener: View.OnLongClickListener = View.OnLongClickListener {

        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition

        //'DO U RLY WANT TO DELETE?' dialog

        var builder = AlertDialog.Builder(this)
        builder.setTitle("Do you want to delete task?")

        builder.setPositiveButton("OK") { _, _ ->

            // delete the task from DB
            db.collection(tasksCollection).document(tasks[pos].id)
                    .delete()
                    .addOnSuccessListener {
                        tasks.removeAt(pos)
                        Log.d(TAG, "task successfully deleted from DB!")

                        // TODO: make it safe
                        recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting task", e) }
        }

        builder.setNegativeButton("CANCEL") { _, _ ->
            Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()
        }

        builder.show()

        true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_board)

//        val eboard: String = intent.getStringExtra("board")
//
//        if (eboard == null) {
//            this.board = DEFAULT_BOARD
//        } else {
//            this.board = eboard
//        }

        board = DEFAULT_BOARD

        toolbar = findViewById(R.id.toolbar_board)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        fabAddTask = findViewById(R.id.fabAddTask)
        fabAddTask.setOnClickListener {
            Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()

            // invoke LibActivity

            var newIntent = Intent(this, LibActivity::class.java)
            newIntent.putExtra("mode", "ADD")
            newIntent.putExtra("board", DEFAULT_BOARD)
            startActivity(newIntent)
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
                            .whereEqualTo("board", DEFAULT_BOARD)
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
                                    recyclerView.addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.default_padding).toInt()))

                                    val myAdapter = TaskAdapter(this, tasks)
                                    recyclerView.adapter = myAdapter

                                    myAdapter.setOnItemClickListener(onItemClickListener)
                                    myAdapter.setOnItemLongClickListener(onItemLongClickListener)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.switch_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.action_switch) {
            val newIntent = Intent(this, LibActivity::class.java)
            newIntent.putExtra("mode", "LIB")
            newIntent.putExtra("board", board)
            startActivity(newIntent)
        }

        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        // Handle navigation view item clicks here.
        val id = item.itemId

        when (id) {
            R.id.nav_board1 -> {
                // Handle the camera action
            }
            R.id.nav_board2 -> {

            }
            R.id.nav_board3 -> {

            }
            R.id.nav_board4 -> {

            }
            R.id.nav_add -> {

            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        // nothing
    }
}