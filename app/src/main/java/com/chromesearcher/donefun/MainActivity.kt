package com.chromesearcher.donefun

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val tasks: ArrayList<Task> = ArrayList()
    private val types: ArrayList<TaskType> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.tasks_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = FirebaseFirestore.getInstance()



//        val docRef = db.collection("taskTypes").document("8wyZTvgwtMtVnssHEHC4")
//        docRef.get().addOnSuccessListener { documentSnapshot ->
//            val taskType = documentSnapshot.toObject(TaskType::class.java)
//
//            tasks.add(Task("IN PROGRESS", taskType as TaskType))
//
//        }

        val source = Source.DEFAULT

        val TAG = "myLogs"

        val docRef = db.collection("users").document("bro")


        docRef.get(source).addOnCompleteListener {task ->

            if(task.isSuccessful) {
                val document = task.result!!
                Log.d(TAG, "Cached document data: ${document.data}")
            } else {
                Log.d(TAG, "Cached get failed: ", task.exception)
            }

        }

        db.collection("taskTypes").get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    Log.d(TAG, "${doc.id} => ${doc.data}")
                    val id = (doc.data["iconId"] as String).toInt()
                    val name = doc.data["name"] as String
                    Log.d(TAG,"${id!!::class.simpleName}")    // ... only class name
                    Log.d(TAG, id.toString())
                    Log.d(TAG, name)

                    var iconId: Int = 0

                    when (id) {
                        0 -> iconId = R.drawable.ic_baseline_build_24px
                        1 -> iconId = R.drawable.ic_shopping_cart_black_24dp
                        2 -> iconId = R.drawable.ic_wc_black_24dp
                    }

                    types.add(TaskType(iconId, name))
                    System.out.println("test it")
                }

                addTasks()
                recyclerView.adapter = TaskAdapter(this, tasks)
            }
            .addOnFailureListener {exc ->
                Log.w(TAG, "ERROORRRR: ", exc)
            }

        System.out.println("GO GET IT")
//        addTypes()
//

//        addTasks()
//        addTasks()

//        tasks.add(Task("IN PROGRESS", taskType))

    }

    private fun addTypes() {

        val build = R.drawable.ic_baseline_build_24px
        val shopping = R.drawable.ic_shopping_cart_black_24dp
        val wc = R.drawable.ic_wc_black_24dp

        types.add(TaskType(R.drawable.ic_baseline_build_24px, "wash"))
        types.add(TaskType(R.drawable.ic_shopping_cart_black_24dp, "homework"))
        types.add(TaskType(R.drawable.ic_wc_black_24dp, "dickkicking"))



        types.add(TaskType(build, "fix stuff"))
        types.add(TaskType(wc, "dry the gloryhole"))
        types.add(TaskType(wc, "three naryada wne ocheredy"))


        types.add(TaskType(R.drawable.ic_baseline_build_24px, "ZAWOD"))
        types.add(TaskType(shopping, "buy new brother"))
        types.add(TaskType(R.drawable.ic_wc_black_24dp, "incest"))
    }

    private fun addTasks() {


        tasks.add(Task("IN PROGRESS", types[0]))
        tasks.add(Task("IN PROGRESS", types[1]))
        tasks.add(Task("IN PROGRESS", types[2]))
//        tasks.add(Task("IN PROGRESS", types[3]))
//        tasks.add(Task("IN PROGRESS", types[4]))
//        tasks.add(Task("IN PROGRESS", types[5]))
//        tasks.add(Task("IN PROGRESS", types[6]))
//        tasks.add(Task("IN PROGRESS", types[7]))
//        tasks.add(Task("IN PROGRESS", types[8]))
        tasks.add(Task("DONE", types[0]))

    }
}
