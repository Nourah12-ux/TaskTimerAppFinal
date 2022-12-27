package com.example.timertaskapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.timertaskapp.databinding.ActivityAddTaskBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddTask : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            val taskName = binding.editTaskName.text.toString()
            val taskDetails = binding.editTaskDetails.text.toString()
            if (taskName.isNotEmpty() && taskDetails.isNotEmpty()) {
                val task = Task("",taskName, taskDetails, 0,false)
                addTask(task)
            }
            else
                Toast.makeText(this, "You must fill all filed", Toast.LENGTH_SHORT).show()

        }
        binding.btnCancel.setOnClickListener{
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)

        }


    }

    private fun addTask(model: Task) {

        db.collection("Tasks")
            .add(model)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
                Toast.makeText(this, "Task Added Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()

            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }


}