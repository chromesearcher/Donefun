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
    private val templates: ArrayList<TaskTemplate> = ArrayList()

    private val defaultUser: String = "bro"

    private val TAG: String = "myLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.tasks_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = FirebaseFirestore.getInstance()
        val source = Source.DEFAULT


//        val docRef = db.collection("taskTypes").document("8wyZTvgwtMtVnssHEHC4")
//        docRef.get().addOnSuccessListener { documentSnapshot ->
//            val taskType = documentSnapshot.toObject(TaskTemplate::class.java)
//
//            tasks.add(Task("IN PROGRESS", taskType as TaskTemplate))
//
//        }

//        val docRef = db.collection("boards").document("main")
//
//
//        docRef.get(source).addOnCompleteListener {task ->
//
//            if(task.isSuccessful) {
//                val document = task.result!!
//                Log.d(TAG, "Cached document data: ${document.data}")
//            } else {
//                Log.d(TAG, "Cached get failed: ", task.exception)
//            }
//
//        }

        db.collection("taskTypes").get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    Log.d(TAG, "${doc.id} => ${doc.data}")
                    val iconId = (doc.data["iconId"] as String).toInt()
                    val name = doc.data["name"] as String
                    Log.d(TAG,"${iconId!!::class.simpleName}")    // ... only class name
                    Log.d(TAG, iconId.toString())
                    Log.d(TAG, name)

                    val id = doc.id as String

                    var icon: Int = 0

                    when (iconId) {
                        0 -> icon = R.drawable.ic_baseline_build_24px
                        1 -> icon = R.drawable.ic_shopping_cart_black_24dp
                        2 -> icon = R.drawable.ic_wc_black_24dp
                    }

                    templates.add(TaskTemplate(icon, name, id))
                    System.out.println("test it")
                }

                db.collection("taskInstances")
                    .whereEqualTo("board", "main")
                    .get()
                    .addOnSuccessListener { docs ->
                        for (doc in docs) {
                            Log.d(TAG, "${doc.id} => ${doc.data}")

                            val status = doc.data["status"] as String
                            val typeId = doc.data["typeId"] as String

                            for (t in templates) {
                                Log.d(TAG, t.id)
                                Log.d(TAG, typeId)
                                if (t.id.equals(typeId)) {
                                    tasks.add(Task(status, t))
                                }
                            }

                            recyclerView.adapter = TaskAdapter(this, tasks)
                        }
                    }
                    .addOnFailureListener { exc ->
                        Log.w(TAG, "Error getting documents: ", exc)
                    }
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

//    private fun addTypes() {
//
//        val build = R.drawable.ic_baseline_build_24px
//        val shopping = R.drawable.ic_shopping_cart_black_24dp
//        val wc = R.drawable.ic_wc_black_24dp
//
//        templates.add(TaskTemplate(R.drawable.ic_baseline_build_24px, "wash"))
//        templates.add(TaskTemplate(R.drawable.ic_shopping_cart_black_24dp, "homework"))
//        templates.add(TaskTemplate(R.drawable.ic_wc_black_24dp, "dickkicking"))
//
//
//
//        templates.add(TaskTemplate(build, "fix stuff"))
//        templates.add(TaskTemplate(wc, "dry the gloryhole"))
//        templates.add(TaskTemplate(wc, "three naryada wne ocheredy"))
//
//
//        templates.add(TaskTemplate(R.drawable.ic_baseline_build_24px, "ZAWOD"))
//        templates.add(TaskTemplate(shopping, "buy new brother"))
//        templates.add(TaskTemplate(R.drawable.ic_wc_black_24dp, "incest"))
//    }

    private fun addTasks() {


        tasks.add(Task("IN PROGRESS", templates[0]))
        tasks.add(Task("IN PROGRESS", templates[1]))
        tasks.add(Task("IN PROGRESS", templates[2]))
//        tasks.add(Task("IN PROGRESS", templates[3]))
//        tasks.add(Task("IN PROGRESS", templates[4]))
//        tasks.add(Task("IN PROGRESS", templates[5]))
//        tasks.add(Task("IN PROGRESS", templates[6]))
//        tasks.add(Task("IN PROGRESS", templates[7]))
//        tasks.add(Task("IN PROGRESS", templates[8]))
        tasks.add(Task("DONE", templates[0]))

    }
}
