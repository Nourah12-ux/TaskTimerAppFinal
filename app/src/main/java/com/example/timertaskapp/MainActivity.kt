package com.example.timertaskapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.timertaskapp.databinding.ActivityMainBinding
import com.example.timertaskapp.databinding.ItemRowBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

//@SuppressLint("NotifyDataSetChanged")  //Indicates that Lint should ignore the specified warnings for the annotated element
class MainActivity : AppCompatActivity() ,TaskAdapter.ClickListener{
    private lateinit var binding: ActivityMainBinding
private lateinit var bindingTask: ItemRowBinding
    val db = Firebase.firestore
    private var total=0L
    private var listTasks= mutableListOf<Task>()
    private lateinit var showTaskDetails: ShowTaskDetails
    lateinit var taskUpdate: Task


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getTask()

        taskUpdate= Task("","","",0L,false)
        binding.btnAdd.setOnClickListener{
            startActivity(Intent(this,AddTask::class.java))
        }

        showTaskDetails= ShowTaskDetails()
        bindingTask=ItemRowBinding.inflate(layoutInflater)
    }
    private fun getTask(){
        total=0L
        db.collection("Tasks")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("TAG", "${document.id} => ${document.data}")

                    val task=Task(
                        document.id,
                        document.getString("taskName").toString(),
                        document.getString("taskDetails").toString(),
                        document.getLong("timeSpent")!!.toLong(),
                        document.getBoolean("flag").toString().toBoolean()
//                      document.getString("flag").toBoolean()
                    )
                    listTasks.add(task)
                    total+=task.timeSpent

//                    if (task==taskUpdate)
//                    {
//                        id=document.id
//                    }
                }
                binding.rvTasks.adapter=TaskAdapter(this,this,listTasks)

                binding.tvTimer.text="Total: ${getFormattedStopWatch(total)}"
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents.", exception)
            }
    }

    private fun delete(taskName: String) {
        total = 0L
        db.collection("Tasks")
            .whereEqualTo("taskName", taskName)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val id = document.id
                    db.collection("Tasks")
                        .document(id)
                        .delete()
                        .addOnSuccessListener {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                listTasks.removeIf { it.taskName == taskName }
                                binding.rvTasks.adapter?.notifyDataSetChanged()
                            }

                            for (task in listTasks) {
                                total += task.timeSpent
                                binding.tvTimer.text = "Total: ${getFormattedStopWatch(total)}"
                            }

                        }
                        .addOnFailureListener { e ->
                            Log.w("TAG", "Error adding document", e)
                        }
                }
            }
    }

    override fun onDelete(task: Task) {
        delete(task.taskName)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun openTask(task: Task) {
        var intent = Intent(this, ShowTaskDetails::class.java)
                intent.putExtra("Task", task)
                startActivity(intent)
    }

    override fun updateTime(totalTime: String) {
        binding.tvTimer.text="Total Time : $totalTime"
    }


}


