package com.chromesearcher.donefun

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.database.ServerValue


class LibActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    // source board which asked for new Tasks (used in ADD mode)
    // thats boardId, not boardName
    private lateinit var board: String
    // curr user, also userId, not name or smth else
    private lateinit var user: String
    private var nSelected: Int = 0

    private val templates: ArrayList<TaskTemplate> = ArrayList()

    private val TAG: String = "myLogs"
    private var addMode: Boolean = false

    private val tasksCollection: String = "tasks"
    private val templatesCollection: String = "taskTypes"

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val source = Source.DEFAULT

    private val onItemClickListener: View.OnClickListener = View.OnClickListener{

        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition
        val item: SelectableTaskTemplate = templates[pos] as SelectableTaskTemplate

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

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            input.setText(oldText)

            var builder = AlertDialog.Builder(this)
            builder.setTitle("Edit template")
            builder.setView(input)
            builder.setPositiveButton("OK") { _, _ ->
                // push data to DB (update template data)
                db.collection(templatesCollection).document(oldId)
                        .update("name", input.text.toString())
                        .addOnSuccessListener {
                            val newTemplate = SelectableTaskTemplate(input.text.toString(), oldId, false)
                            templates.set(pos, newTemplate)

                            Log.d(TAG, "DocumentSnapshot (template) successfully written!")

                            // TODO: make it safe
                            recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing template", e) }
            }
            builder.setNegativeButton("CANCEL") { _, _ -> }
            builder.show()
        }
    }

    private val onItemLongClickListener: View.OnLongClickListener = View.OnLongClickListener {
        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition

        if (addMode) { /* not implemented */ }
        else { // flow LIB
            //'DO U RLY WANT TO DELETE?' dialog
            var builder = AlertDialog.Builder(this)
            builder.setTitle("Do you want to delete template?")
            builder.setPositiveButton("OK") { _, _ ->
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
            builder.setNegativeButton("CANCEL") { _, _ -> }
            builder.show()
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
        user = intent.getStringExtra("user")

        initRecyclerView()
        downloadTemplates()

        initFab()
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.task_template_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.default_padding).toInt()))
        recyclerView.setHasFixedSize(true)

        val myAdapter = TaskTemplateAdapter(this, templates)
        recyclerView.adapter = myAdapter

        myAdapter.setOnItemClickListener(onItemClickListener)
        myAdapter.setOnItemLongClickListener(onItemLongClickListener)
    }

    private fun downloadTemplates() {
        // Acquire Templates (all)
        db.collection(templatesCollection).get(source)
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    val text = doc.data["name"] as String
                    val id = doc.id

                    templates.add(SelectableTaskTemplate(text, id, false))
                }

                // TODO: make it safe
                recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
            }
            .addOnFailureListener {exc ->
                Log.w(TAG, "ERROR getting templates: ", exc)
            }
    }

    private fun initFab() {
        fabAdd = findViewById(R.id.fabAdd)
        fabAdd.setOnClickListener {
            if (addMode) { // ADD TASK flow
                // DO YOU REALLY WANNA SUBMIT CHOICE' dialog
                var builder = AlertDialog.Builder(this)
                builder.setTitle("Do you want to submit choice?")
                builder.setPositiveButton("OK") { _, _ ->
                    for (template in templates) {
                        if ((template as SelectableTaskTemplate).selected) {
                            // push new task to DB
                            val data = HashMap<String, Any>()
                            data["ownerId"] = user
                            data["boardId"] = board
                            data["status"] = "IN PROGRESS"

                            val temp = HashMap<String, String>()
                            temp["id"] = template.id
                            temp["name"] = template.text
                            data["template"] = temp

//                            val dateCreated = ServerValue.TIMESTAMP
//                            data["date_created"] = dateCreated

                            db.collection(tasksCollection)
                                .add(data)
                                .addOnSuccessListener { docRef ->
                                    Log.d(TAG, "DocumentSnapshot (task) written with ID: ${docRef.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding new Task", e)
                                }
                        }
                    }

                    // TODO: do we need to wait until all ADD TO DB threads are over?
                    // return to Board screen
                    var newIntent = Intent()
                    setResult(RESULT_OK, newIntent)
                    finish() // back to previous activity
                }
                builder.setNegativeButton("CANCEL") { _, _ -> }
                builder.show()

            } else { // LIB flow
                var builder = AlertDialog.Builder(this)
                builder.setTitle("Add template")

                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)
                builder.setPositiveButton("OK") { _, _ ->
                    val data = HashMap<String, Any>()
                    data["name"] = input.text.toString()

                    // push new data to DB
                    db.collection(templatesCollection)
                        .add(data)
                        .addOnSuccessListener { docRef ->
                            templates.add(SelectableTaskTemplate( input.text.toString(), docRef.id, false))

                            // TODO: make it safe
                            recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                        }
                }
                builder.setNegativeButton("CANCEL") { _, _ -> }
                builder.show()
            }
        }
    }

    override fun onBackPressed() {
        var newIntent = Intent()
        setResult(RESULT_OK, newIntent)
        finish()
    }
}