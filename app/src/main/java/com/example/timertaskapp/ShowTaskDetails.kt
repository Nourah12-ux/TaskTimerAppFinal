package com.example.timertaskapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.timertaskapp.databinding.ActivityMainBinding
import com.example.timertaskapp.databinding.ActivityShowTaskDetailsBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class ShowTaskDetails : AppCompatActivity() {
    private lateinit var binding:ActivityShowTaskDetailsBinding

    private val mInterval = 1000 // 1 second in this case
    private var mHandler: Handler? = null
    private var timeInSeconds = 0L
    private var startButtonClicked = false
    var timer = ""
    private var taskName = ""
    private var taskId=""
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowTaskDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initStopWatch()

        val data: Task = intent.getSerializableExtra("Task") as Task// get the data that send from main activity

        taskName = data.taskName
        timeInSeconds = data.timeSpent
        taskId = data.id

            binding.apply {
                tvTitle.text = taskName
                tvContent.text = data.taskDetails
                tvTimer.text = getFormattedStopWatch(data.timeSpent)

                btnStart.setOnClickListener {

                    startOrStopButtonClicked()
                }
                btnReset.setOnClickListener {
                    resetTimerView()
                }
                btnBack.setOnClickListener {
                    stopTimer()
                    update(taskId,timeInSeconds)
                    startButtonClicked = false
                    var intentBack = Intent(this@ShowTaskDetails, MainActivity::class.java)
                    startActivity(intentBack)
                }
            }

        }



    private fun initStopWatch() {
        binding.tvTimer.text = "00:00:00"
    }

    private fun startOrStopButtonClicked() {
        if (!startButtonClicked) {
            startTimer()
            startTimerView()
        } else {
            stopTimer()
            stopTimerView()
        }

    }



    private fun startTimer() {
        mHandler = Handler(Looper.getMainLooper())
        mStatusChecker.run()
    }

    private fun startTimerView() {
        binding.btnStart.text = "stop"
        startButtonClicked = true
    }

    private fun stopTimer() {
        mHandler?.removeCallbacks(mStatusChecker)
    }

    private fun stopTimerView() {
        binding.btnStart.text = "resume"
        update(taskId,timeInSeconds)
        startButtonClicked = false
    }

    private var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                timeInSeconds += 1

                timer=getFormattedStopWatch(timeInSeconds)

                binding.tvTimer.text = timer.toString()

            } finally {

                mHandler!!.postDelayed(this, mInterval.toLong())
            }
        }
    }

    private fun resetTimerView() {
        timeInSeconds = 0
        startButtonClicked = false
        binding.btnStart.text = "start"
        update(taskId,timeInSeconds)
        initStopWatch()
    }



    private fun update(taskId: String, newTime: Long) {

        db.collection("Tasks")
            .document(taskId)
            .update("timeSpent", newTime)
            .addOnSuccessListener {
                Log.w("TAG", "Updated Successfully for $taskId")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

}

