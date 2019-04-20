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

class BoardsListActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddBoard: FloatingActionButton

    private lateinit var user: String

    private val boards: ArrayList<Board> = ArrayList()

    private val db = FirebaseFirestore.getInstance()
    private val source = Source.DEFAULT // TODO: use this in DB calls

    private val boardsCollection: String = "boards"

    private val TAG: String = "myLogs"

    private val onItemClickListener: View.OnClickListener = View.OnClickListener{

        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition
        val item: Board = boards[pos]

        val oldId = item.id
        val oldActor = item.actor
        val oldName = item.name

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(oldName)

        var builder = AlertDialog.Builder(this)
        builder.setTitle("Edit board name")
        builder.setView(input)
        builder.setPositiveButton("OK") { _, _ ->
            // push data to DB (update board data)
            db.collection(boardsCollection).document(oldId)
                    .update("name", input.text.toString())
                    .addOnSuccessListener {
                        val newBoard = Board(input.text.toString(), oldActor, oldId)
                        boards.set(pos, newBoard)

                        Log.d(TAG, "DocumentSnapshot (board) successfully written!")

                        // TODO: make it safe
                        recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing board", e) }
        }
        builder.setNegativeButton("CANCEL") { _, _ ->
            Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    // long tap allows to delete chosen board from the board list (and from DB)
    private val onItemLongClickListener: View.OnLongClickListener = View.OnLongClickListener {

        val viewHolder: RecyclerView.ViewHolder = it.tag as RecyclerView.ViewHolder
        var pos = viewHolder.adapterPosition

        //'DO U RLY WANT TO DELETE?' dialog
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Do you want to delete board?")
        builder.setPositiveButton("OK") { _, _ ->

            // delete the board from DB
            db.collection(boardsCollection).document(boards[pos].id)
                    .delete()
                    .addOnSuccessListener {
                        boards.removeAt(pos)
                        Log.d(TAG, "board successfully deleted from DB!")

                        // TODO: make it safe
                        recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting board", e) }
        }
        builder.setNegativeButton("CANCEL") { _, _ ->
            Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()
        }
        builder.show()

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boards_list)

        user = intent.getStringExtra("user")


        fabAddBoard = findViewById(R.id.fabAddBoard)
        fabAddBoard.setOnClickListener {

            var builder = AlertDialog.Builder(this)
            builder.setTitle("Add new board")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            builder.setPositiveButton("OK") { _, _ ->
                val data = HashMap<String, Any>()
                data["actor"] = user
                data["name"] = input.text.toString()

                // push new data to DB
                db.collection(boardsCollection)
                        .add(data)
                        .addOnSuccessListener { docRef ->
                            boards.add(Board(input.text.toString(), user, docRef.id))

                            // TODO: make it safe
                            recyclerView.adapter!!.notifyDataSetChanged() // danger, adapter may be null in come cases
                        }
            }
            builder.setNegativeButton("CANCEL") { _, _ ->
                Toast.makeText(applicationContext, "FUK U", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }

        db.collection(boardsCollection)
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs) {
                        val actor = doc.data["actor"] as String
                        val name = doc.data["name"] as String
                        val id = doc.id

                        // TODO: filter by user
                        boards.add(Board(name, actor, id))
                    }

                    recyclerView = findViewById(R.id.boards_rv)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.default_padding).toInt()))

                    val myAdapter = BoardAdapter(this, boards)
                    recyclerView.adapter = myAdapter

                    myAdapter.setOnItemClickListener(onItemClickListener)
                    myAdapter.setOnItemLongClickListener(onItemLongClickListener)
                }
                .addOnFailureListener { exc ->
                    Log.w(TAG, "Error getting boards: ", exc)
                }
    }

    override fun onBackPressed() {
        var newIntent = Intent()
        setResult(RESULT_OK, newIntent)
        finish()
    }
}