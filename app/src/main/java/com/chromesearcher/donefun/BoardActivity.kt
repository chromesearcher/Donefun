package com.chromesearcher.donefun

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
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


class BoardActivity : AppCompatActivity() /* TODO: uncomment , NavigationView.OnNavigationItemSelectedListener*/ {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var toolbar: Toolbar
    // TODO: uncomment
//    private lateinit var drawerMenu: Menu

    private lateinit var user: String
    private lateinit var board: String // this is boardId actually, not boardName

    private val tasks: ArrayList<Task> = ArrayList()
//    private val boards: ArrayList<Board> = ArrayList()

    private val tasksCollection: String = "tasks"
    private val boardsCollection: String = "boards"
//    private val usersCollection: String = "users"

    private val db = FirebaseFirestore.getInstance()
    private val source = Source.DEFAULT // TODO: use this in DB calls

    private val TAG: String = "myLogs"
    private val REQUEST_CODE_LIB_ADD = 1
    private val REQUEST_CODE_LIB_FLOW = 2
    private val REQUEST_CODE_BOARDS_LIST = 3
    private val REQUEST_CODE_BOARD = 4

    private val onItemClickListener: View.OnClickListener = View.OnClickListener{

        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition
        val item: Task = tasks[pos]

        val id = item.id
        val status = item.status
        val name = item.name
//        val dateCreated = item.dateCreated

        // TODO: enums
        val newStatus = if (status == "IN PROGRESS") "DONE" else "IN PROGRESS"

        // DB interaction (update status)
        db.collection(tasksCollection).document(id)
                .update("status", newStatus)
                .addOnSuccessListener {
                    val newTask = Task(newStatus, name, id)
                    tasks[pos] = newTask

                    Log.d(TAG, "DocumentSnapshot (task) successfully written|updated!")
                    // TODO: make it safe
                    recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing|updating task", e) }
    }

    // long tap allows to delete chosen task from current board (and from DB)
    private val onItemLongClickListener: View.OnLongClickListener = View.OnLongClickListener {
        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        val pos = viewHolder.adapterPosition

        //'DO U RLY WANT TO DELETE?' dialog
        val builder = AlertDialog.Builder(this)
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
        builder.setNegativeButton("CANCEL") { _, _ -> }
        builder.show()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        user = intent.getStringExtra("user")

        // TODO: download users and check if cur user exists
        // TODO: if exists, download his boards (by default only main)
        // TODO: download tasks for this board
        // TODO: if user don't exist, create board for him (main)

//        val docRef = db.collection(usersCollection).document(user)
//        docRef.get()
//            .addOnSuccessListener { document ->
//                if (document != null) {
//                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
//
//
//
//                } else {
//                    Log.d(TAG, "No such document")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d(TAG, "get failed with downloading user document", exception)
//            }

        board = "default"
        this.title = "main"
        db.collection(boardsCollection).whereEqualTo("ownerId", user)
                .get()
                .addOnSuccessListener { docs ->

                    if (docs.size() == 0) {

                        // create main board
                        val boardName = "main"

                        // push new board to DB
                        val data = HashMap<String, Any>()
                        data["name"] = boardName
                        data["ownerId"] = user

                        db.collection(boardsCollection)
                            .add(data)
                            .addOnSuccessListener { docRef ->
                                board = docRef.id
                                initFab()
                                Log.d(TAG, "DocumentSnapshot (board) written with ID: ${docRef.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding new board", e)
                            }

                    } else {

                        for (doc in docs) {
                            val boardName = doc.data["name"] as String
                            val boardId = doc.id

                            if (boardName == "main") {
                                // download tasks
                                board = boardId
                                initFab()
                                downloadTasks(boardId)
                            }
                        }
                    }
                }
                .addOnFailureListener { exc ->
                    Log.w(TAG, "Error getting boards: ", exc)
                }

        toolbar = findViewById(R.id.toolbar_board)
        setSupportActionBar(toolbar)

        // TODO: uncomment
//        initDrawer()
//        initDrawerMenu()
//        downloadBoards()

        initRecyclerView()
    }

    private fun initFab() {
        fabAddTask = findViewById(R.id.fabAddTask)
        fabAddTask.setOnClickListener {
            // invoke LibActivity
            var newIntent = Intent(this, LibActivity::class.java)
            newIntent.putExtra("mode", "ADD")
            newIntent.putExtra("board", board)
            newIntent.putExtra("user", user)
            startActivityForResult(newIntent, REQUEST_CODE_LIB_ADD)
        }
    }

    // TODO: uncomment
//    private fun initDrawer() {
//        // DRAWER setup
//        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
//
//        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
//        drawer.addDrawerListener(toggle)
//        toggle.syncState()
//
//        val navigationView = findViewById<NavigationView>(R.id.nav_view)
//        navigationView.setNavigationItemSelectedListener(this)
//
//        drawerMenu = navigationView.menu
//    }

    // TODO: uncomment
//    private fun initDrawerMenu() {
//        // init menu (with boards)
//        val item1 = drawerMenu.findItem(R.id.nav_board1)
//        val item2 = drawerMenu.findItem(R.id.nav_board2)
//        val item3 = drawerMenu.findItem(R.id.nav_board3)
//
//        when(boards.size) {
//            0 -> {
//                // TODO: change to: item1.setVisible(false)
//                item1.title = "<no board>"
//                item2.title = item1.title
//                item3.title = item1.title
//            }
//            1 -> {
//                item1.title = boards[0].name
//                item2.title = "<no board>"
//                item3.title = item2.title
//            }
//            2 -> {
//                item1.title = boards[0].name
//                item2.title = boards[1].name
//                item3.title = "<no board>"
//            }
//            else -> {
//                item1.title = boards[0].name
//                item2.title = boards[1].name
//                item3.title = boards[2].name
//            }
//        }
//    }

    private fun initRecyclerView() {
        // init recyclerview (with tasks)
        recyclerView = findViewById(R.id.tasks_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.default_padding).toInt()))

        // TODO: play around the animator (it may slow down the RV work)
        val itemAnimator = DefaultItemAnimator()
        itemAnimator.addDuration = 1000
        itemAnimator.removeDuration = 1000
        recyclerView.itemAnimator = itemAnimator

        val myAdapter = TaskAdapter(this, tasks)
        recyclerView.adapter = myAdapter

        myAdapter.setOnItemClickListener(onItemClickListener)
        myAdapter.setOnItemLongClickListener(onItemLongClickListener)
    }

// TODO: uncomment

//    private fun refreshBoards() {
//        boards.clear()
//        downloadBoards()
//    }
//
//    private fun downloadBoards() {
//        // Acquire BOARDS
//        db.collection(boardsCollection).whereEqualTo("actor", user)
//                .get()
//                .addOnSuccessListener { docs ->
//                    for (doc in docs) {
//                        val actor = doc.data["actor"] as String
//                        val name = doc.data["name"] as String
//                        val id = doc.id
//
//                        if (id == board) {
//                            this.title = name
//                        }
//
//                        // TODO: add filter by user
//                        boards.add(Board(name, actor, id))
//                    }
//
//                    // TODO: notify menu (reinit)
//                    // TODO: uncomment
////                    initDrawerMenu()
//                }
//                .addOnFailureListener { exc ->
//                    Log.w(TAG, "Error getting boards: ", exc)
//                }
//    }

    private fun refreshTasks() {
        // TODO: may be optimized
        tasks.clear()
        downloadTasks(board)
    }

    private fun downloadTasks(boardId: String) {

        db.collection(tasksCollection)
            .whereEqualTo("boardId", boardId)//.orderBy("date_created")
            .whereEqualTo("ownerId", user)
            .get()
            .addOnSuccessListener { docsT ->
                for (doc in docsT) {
                    val status = doc.data["status"] as String
                    val template = doc.data["template"] as Map<String, String>
                    val name = template["name"] as String
//                    val dateCreated = doc.data["date_created"] as Map<String, String>
                    val id = doc.id

//                    Log.d(TAG, dateCreated[".sv"]) // "timestamp" always
//
//                    for (t in templates) {
//                        if (t.id == typeId) {
//                            tasks.add(Task(status, t, id, dateCreated))
//                        }
//                    }

                    tasks.add(Task(status, name, id))
                }

                // refresh recyclerview with loaded data
                // TODO: make it safe
                recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
            }
            .addOnFailureListener { exc ->
                Log.w(TAG, "Error getting tasks: ", exc)
            }
    }

//    private fun downloadTasksAndTemplates() {
//        // TODO: download templates only for current user
//        db.collection(templatesCollection)
//                .get()
//                .addOnSuccessListener { docs ->
//                    for (doc in docs) {
//                        val iconId = (doc.data["iconId"] as String).toInt()
//                        val name = doc.data["name"] as String
//                        val id = doc.id
//                        var icon = 0
//
//                        when (iconId) {
//                            0 -> icon = R.drawable.ic_baseline_build_24px
//                            1 -> icon = R.drawable.ic_shopping_cart_black_24dp
//                            2 -> icon = R.drawable.ic_wc_black_24dp
//                        }
//
//                        templates.add(TaskTemplate(icon, name, id))
//                    }
//
//                    db.collection(tasksCollection)
//                            .whereEqualTo("board", board).orderBy("date_created")
//                            .get()
//                            .addOnSuccessListener { docsT ->
//                                for (doc in docsT) {
//                                    val status = doc.data["status"] as String
//                                    val typeId = doc.data["typeId"] as String
//                                    val dateCreated = doc.data["date_created"] as Map<String, String>
//                                    val id = doc.id
//
//                                    Log.d(TAG, dateCreated[".sv"]) // "timestamp" always
//
//                                    for (t in templates) {
//                                        if (t.id == typeId) {
//                                            tasks.add(Task(status, t, id, dateCreated))
//                                        }
//                                    }
//                                }
//
//                                // refresh recyclerview with loaded data
//                                // TODO: make it safe
//                                recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
//                            }
//                            .addOnFailureListener { exc ->
//                                Log.w(TAG, "Error getting tasks: ", exc)
//                            }
//                }
//                .addOnFailureListener {exc ->
//                    Log.w(TAG, "Error getting taskTypes: ", exc)
//                }
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.switch_menu, menu)

//        this.menu = menu as Menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.action_switch) {
            val newIntent = Intent(this, LibActivity::class.java)
            newIntent.putExtra("mode", "LIB")
            newIntent.putExtra("board", board)
            newIntent.putExtra("user", user)
            startActivityForResult(newIntent, REQUEST_CODE_LIB_FLOW)
        }
        return true
    }

    // TODO: uncomment
//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//
//        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
//
//        // Handle navigation view item clicks here.
//        val id = item.itemId
//
//        var boardId = 0
//
//        when (id) {
//            R.id.nav_board1 -> {
//
//                if (boards.size == 0) {
//                    drawer.closeDrawer(GravityCompat.START)
//                    return true
//                }
//
//                // go to board 1
//                boardId = 0
//            }
//            R.id.nav_board2 -> {
//
//                if (boards.size == 1) {
//                    drawer.closeDrawer(GravityCompat.START)
//                    return true
//                }
//
//                // go to board 2
//                boardId = 1
//            }
//            R.id.nav_board3 -> {
//
//                if (boards.size == 2) {
//                    drawer.closeDrawer(GravityCompat.START)
//                    return true
//                }
//
//                // go to board 3
//                boardId = 2
//            }
//
//            R.id.nav_all_boards -> {
//                // go to boards screen
//
//                var newIntent = Intent(this, BoardsListActivity::class.java)
//                newIntent.putExtra("user", user)
//                newIntent.putExtra("board", board)
//                startActivityForResult(newIntent, REQUEST_CODE_BOARDS_LIST)
//
//                drawer.closeDrawer(GravityCompat.START)
//                return true
//            }
//        }
//
//        if (boards[boardId].id == board) {
//            drawer.closeDrawer(GravityCompat.START)
//            return true
//        }
//
//        // TODO: get it from memory, not create activity once again
//        var newIntent = Intent(this, BoardActivity::class.java)
//        newIntent.putExtra("board", boards[boardId].id)
//        startActivityForResult(newIntent, REQUEST_CODE_BOARD)
//
//        drawer.closeDrawer(GravityCompat.START)
//        return true
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_BOARD -> {
                    // TODO: check that no actions required
                    // TODO: actually cut out this case in future
                }
                // TODO: uncomment
//                REQUEST_CODE_BOARDS_LIST -> {
//
//                    val isDeleted = data?.getBooleanExtra("is_deleted", false)
//
//                    // TODO: make safe
//                    if (isDeleted!!) {
//                        // TODO: get it from memory, not create activity once again
//                        // go to main board
//                        var newIntent = Intent(this, BoardActivity::class.java)
//                        newIntent.putExtra("board", DEFAULT_BOARD_ID)
//                        startActivityForResult(newIntent, REQUEST_CODE_BOARD)
//                    } else {
//                        // refresh data (board list in drawer)
//                        refreshBoards()
//                    }
//                }
                REQUEST_CODE_LIB_FLOW -> {
                    // TODO: check that no actions required
                    // may be refresh templates
                }
                REQUEST_CODE_LIB_ADD -> {
                    // refresh data (task list)
                    refreshTasks()
                }
            }
        } else {
            Toast.makeText(this, "Wrong activity result", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        // to prev board?
        var newIntent = Intent()
        setResult(RESULT_OK, newIntent)
        finish()
    }
}