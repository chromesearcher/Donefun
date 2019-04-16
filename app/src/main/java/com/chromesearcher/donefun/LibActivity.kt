package com.chromesearcher.donefun

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast



class LibActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    // source board which asked for new Tasks (used in ADD mode)
    private lateinit var board: String
    private var nSelected: Int = 0


    private val templates: ArrayList<TaskTemplate> = ArrayList()

    private val TAG: String = "myLogs"

    private var addMode: Boolean = false

    private val tasksCollection: String = "taskInstances"
    private val templatesCollection: String = "taskTypes"

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val source = Source.DEFAULT

    private val onItemClickListener: View.OnClickListener = View.OnClickListener{

        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition
        val item: SelectableTaskTemplate = templates[pos] as SelectableTaskTemplate

        Toast.makeText(applicationContext, "FUK U: " + item.text, Toast.LENGTH_SHORT).show()

        if (addMode) { // flow add task

            // flip selection
            item.selected = !item.selected

            if (item.selected) nSelected-- else nSelected++

            // TODO: add action bar | toolbar (now implemented via FAB)

            // TODO: make it safe
            recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases

        } else { // flow LIB

            val oldId = item.id
            val oldText = item.text
            val oldIcon = item.iconId

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            input.setText(oldText)

            var builder = AlertDialog.Builder(this)
            builder.setTitle("Edit template")
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                // Toast.makeText(applicationContext, input.text.toString(), Toast.LENGTH_SHORT).show()

                // push data to DB (update template data)
                db.collection(templatesCollection).document(oldId)
                        .update("name", input.text.toString())
                        .addOnSuccessListener {
                            val newTemplate = SelectableTaskTemplate(oldIcon, input.text.toString(), oldId, false)
                            templates.set(pos, newTemplate)

                            Log.d(TAG, "DocumentSnapshot (template) successfully written!")

                            // TODO: make it safe
                            recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing template", e) }
            }

            builder.setNegativeButton("CANCEL") { _, _ ->
                Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()
            }

            builder.show()
        }
    }

    private val onItemLongClickListener: View.OnLongClickListener = View.OnLongClickListener {

        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition


        if (addMode) { /* not implemented */ }
        else { // flow LIB

            // TODO: add 'DO U RLY WANT TO DELETE?' dialog

            // delete the template from DB
            db.collection(templatesCollection).document(templates[pos].id)
                    .delete()
                    .addOnSuccessListener {
                        templates.removeAt(pos)
                        Log.d(TAG, "template successfully deleted from DB!")

                        // TODO: make it safe
                        recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting template", e) }
        }

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lib)

        var mode: String = intent.getStringExtra("mode")

        if (mode == "ADD") {
            addMode = true
        }

        board = intent.getStringExtra("board")


        db.collection(tasksCollection)
                .whereEqualTo("board", board)
                .get()
                .addOnSuccessListener { docs ->

                    // only IN PROGRESS
                    var taskTypeIds: ArrayList<String> = ArrayList()

                    for (doc in docs) {
                        val typeId = doc.data["typeId"] as String // TODO: null check
                        val status = doc.data["status"] as String // TODO: null check

                        // we should NOT be able to add new tasks which are IN PROGRESS already
                        // therefore we remember which are already in progress and filter them later
                        if (status == "IN PROGRESS") {
                            taskTypeIds.add(typeId)
                        }

                        System.out.println("fuk u")
                    }

                    // Acquire Templates (only not yet Instanciated)
                    db.collection(templatesCollection).get(source)
                            .addOnSuccessListener { docs ->
                                for (doc in docs) {
                                    val iconId = (doc.data["iconId"] as String).toInt()
                                    val text = doc.data["name"] as String

                                    val id = doc.id

                                    var icon: Int = 0

                                    when (iconId) {
                                        0 -> icon = R.drawable.ic_baseline_build_24px
                                        1 -> icon = R.drawable.ic_shopping_cart_black_24dp
                                        2 -> icon = R.drawable.ic_wc_black_24dp
                                    }

                                    if (addMode) {
                                        // filter already instancieted Templates (those which are IN PROGRESS)
                                        // if there is task with cur type id, not add it
                                        // show only DONE or NOT ISTANCIATED types
                                        if (!taskTypeIds.contains(id)) {
                                            templates.add(SelectableTaskTemplate(icon, text, id, false))
                                        }
                                    } else {
                                        templates.add(SelectableTaskTemplate(icon, text, id, false))
                                    }
                                }


                                recyclerView = findViewById(R.id.task_template_rv)
                                recyclerView.layoutManager = LinearLayoutManager(this)

                                val myAdapter = TaskTemplateAdapter(this, templates)
                                recyclerView.adapter = myAdapter

                                myAdapter.setOnItemClickListener(onItemClickListener)
                                myAdapter.setOnItemLongClickListener(onItemLongClickListener)
                            }
                            .addOnFailureListener {exc ->
                                Log.w(TAG, "ERROR getting templates: ", exc)
                            }
                }
                .addOnFailureListener { exc ->
                    Log.w(TAG, "Error getting taskInstances: ", exc)
                }



        fabAdd = findViewById(R.id.fabAdd)

        fabAdd.setOnClickListener {

            if (addMode) {

                for (template in templates) {
                    if ((template as SelectableTaskTemplate).selected) {

                        // push new task to DB

                        val data = HashMap<String, Any>()
                        data["board"] = board
                        data["status"] = "IN PROGRESS"
                        data["typeId"] = template.id

                        db.collection(tasksCollection)
                                .add(data)
                                .addOnSuccessListener { docRef ->
                                    Log.d(TAG, "DocumentSnapshot written with ID: ${docRef.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding new Task", e)
                                }
                    }
                }

                // return to Board screen

                // TODO: add 'DO YOU REALLY WANNA SUBMIT CHOICE' dialog

                // TODO: do we need to wait until all ADD TO DB threads are over?

//                var newIntent = Intent(this, BoardActivity::class.java)
//                startActivity(newIntent)
                finish() // back to previous activity



            } else { // LIB flow

//                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                inputMethodManager.toggleSoftInputFromWindow(
//                    linearLayout.getApplicationWindowToken(),
//                    InputMethodManager.SHOW_FORCED, 0
//                )

                var builder = AlertDialog.Builder(this)
                builder.setTitle("Add template")

                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT

                builder.setView(input)

                builder.setPositiveButton("OK") { _, _ ->
                    // Toast.makeText(applicationContext, input.text.toString(), Toast.LENGTH_SHORT).show()

                    val data = HashMap<String, Any>()
                    data["iconId"] = "0"
                    data["name"] = input.text.toString()

                    // push new data to DB
                    db.collection(templatesCollection)
                            .add(data)
                            .addOnSuccessListener { docRef ->
                                templates.add(SelectableTaskTemplate(0, input.text.toString(), docRef.id, false))

                                // TODO: make it safe
                                recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                            }

                }

                builder.setNegativeButton("CANCEL") { _, _ ->
                    Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()
                }

                builder.show()
            }
        }
    }

    override fun onBackPressed() {


//        var activity: Class<*>? = null
//
//        when (board) {
//            "main" -> activity = BoardActivity::class.java
//        }

//        val newIntent = Intent(this, BoardActivity::class.java)
////        newIntent.putExtra("board", board)
//        startActivity(newIntent)

        finish()
    }
}