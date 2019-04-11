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
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy


class LibActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var tracker: SelectionTracker<Long>

    private val templates: ArrayList<TaskTemplate> = ArrayList()

    private val TAG: String = "myLogs"

    private var addMode: Boolean = false

/*
    private val onItemClickListener: View.OnClickListener = View.OnClickListener{

        val viewHolder: RecyclerView.ViewHolder = it.getTag() as RecyclerView.ViewHolder

        var pos = viewHolder.adapterPosition

        val item = templates.get(pos)
        val oldId = item.id
        val oldText = item.text
        val oldIcon = item.iconId

        Toast.makeText(applicationContext, "FUK U: " + item.text, Toast.LENGTH_SHORT).show()
        /*
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(oldText)

        var builder = AlertDialog.Builder(this)
        builder.setTitle("Edit template")
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            //                Toast.makeText(applicationContext, input.text.toString(), Toast.LENGTH_SHORT).show()

            val newTemplate = TaskTemplate(oldIcon, input.text.toString(), oldId)
            templates.set(pos, newTemplate)

            // TODO: make it safe
            recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
        }

        builder.setNegativeButton("CANCEL") { dialog, which ->
            Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()
        }

        builder.show()

        */
    }

    private val onItemLongClickListener: View.OnLongClickListener = View.OnLongClickListener {

        val viewHolder: RecyclerView.ViewHolder = it.getTag() as RecyclerView.ViewHolder

        var pos = viewHolder.adapterPosition

        // TODO: add 'DO U RLY WANT TO DELETE?' dialog

        templates.removeAt(pos)

        // TODO: make it safe
        recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases

        true
    }
*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lib)


        // Selection code
        if (savedInstanceState != null) {
            tracker?.onRestoreInstanceState(savedInstanceState)
        }

//        val intent = getIntent()
//        var mode: String = intent.getStringExtra("mode")
//
//        if (mode.equals("ADD")) {
//            addMode = true
//        }





        val db = FirebaseFirestore.getInstance()
        val source = Source.DEFAULT

        db.collection("taskTypes").get(source)
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    val iconId = (doc.data["iconId"] as String).toInt()
                    val name = doc.data["name"] as String

                    val id = doc.id as String

                    var icon: Int = 0

                    when (iconId) {
                        0 -> icon = R.drawable.ic_baseline_build_24px
                        1 -> icon = R.drawable.ic_shopping_cart_black_24dp
                        2 -> icon = R.drawable.ic_wc_black_24dp
                    }

                    templates.add(TaskTemplate(icon, name, id))

                }


                recyclerView = findViewById(R.id.task_template_rv)
                recyclerView.layoutManager = LinearLayoutManager(this)

                val myAdapter = TaskTemplateAdapter(this, templates)
                recyclerView.adapter = myAdapter

                tracker = SelectionTracker.Builder<Long>(
                    "selection-1",
                    recyclerView,
                    StableIdKeyProvider(recyclerView),
                    MyLookup(recyclerView),
                    StorageStrategy.createLongStorage()
                ).withSelectionPredicate(SelectionPredicates.createSelectAnything()
                ).build()

                myAdapter.setTracker(tracker)

//                myAdapter.setOnItemClickListener(onItemClickListener)
//                myAdapter.setOnItemLongClickListener(onItemLongClickListener)
            }
            .addOnFailureListener {exc ->
                Log.w(TAG, "ERROR getting templates: ", exc)
            }

        fabAdd = findViewById(R.id.fabAdd)

        fabAdd.setOnClickListener {
//            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodManager.toggleSoftInputFromWindow(
//                linearLayout.getApplicationWindowToken(),
//                InputMethodManager.SHOW_FORCED, 0
//            )

            var builder = AlertDialog.Builder(this)
            builder.setTitle("Add template")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT

            builder.setView(input)

            builder.setPositiveButton("OK") { dialog, which ->
//                Toast.makeText(applicationContext, input.text.toString(), Toast.LENGTH_SHORT).show()

                templates.add(TaskTemplate(0, input.text.toString(), "new"))

                // TODO: make it safe
                recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
            }

            builder.setNegativeButton("CANCEL") { dialog, which ->
                Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()
            }

            builder.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        if (outState != null) {
            tracker?.onSaveInstanceState(outState)
        }
    }

}