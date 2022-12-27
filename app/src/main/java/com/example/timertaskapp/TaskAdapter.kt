package com.example.timertaskapp

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.timertaskapp.databinding.ItemRowBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class TaskAdapter(private var clickListener: ClickListener, var activity: MainActivity , private var list: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {
        inner class MyViewHolder(b: ItemRowBinding) :RecyclerView.ViewHolder(b.root) {
        val binding: ItemRowBinding = b
    }

    private var threadList = mutableListOf<Thread>()
    var totalTime = 0L


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b: ItemRowBinding =
            ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(b)
    }

    private val db = Firebase.firestore

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val data = list[position]


        holder.binding.apply {

                var timeInSeconds = data.timeSpent
                var timeString = ""
                var totalTimeString =""

                tvTitle.text = data.taskName
                tvContent.text = data.taskDetails
                tvTimer.text = getFormattedStopWatch(data.timeSpent)


            val mStatusChecker: Runnable = object : Runnable {
                override fun run() {
                    var boolean = true
                    var totalTime = totalTime
                        try {
                            while (boolean) {
                            Thread.sleep(1000)
                            timeInSeconds += 1
                                totalTime+=1
                                timeString = getFormattedStopWatch(timeInSeconds)
                                totalTimeString = getFormattedStopWatch(totalTime)
                                Log.d("totalTime", "run: $totalTimeString")
                            data.timeSpent = timeInSeconds
                            activity.runOnUiThread { tvTimer.text = timeString
                                clickListener.updateTime(totalTimeString)
                            this@TaskAdapter.notifyItemChanged(position)}
                            if (threadList[position].isInterrupted){
                            return}}

                        } catch (e: Exception) {
                            Log.d("Error", "run: ${e.message}")
                        }
                    }

            }
            threadList.add(Thread(mStatusChecker))


            ivDelete.setOnClickListener {
                clickListener.onDelete(data)
            }
            root.setOnClickListener {
                clickListener.openTask(data)

            }
            tvTimer.setOnClickListener {

                totalTime = 0L

                for (index in 0 until list.size){
                    var task = list[index]
                    if (task!=data){
                        if (task.flag) {
                            threadList[index].interrupt()
                            task.flag = false
                            db.collection("Tasks")
                                .document(task.id).set(task)
                        }
                    }
                    totalTime += task.timeSpent
                }

                if(!data.flag)
                {
                    data.flag = true
                    threadList[position]= Thread(mStatusChecker)
                    threadList[position].start()
                    db.collection("Tasks")
                .document(data.id).update("flag", data.flag)
                }
                else
                {
                    threadList[position].interrupt()
                    data.flag = false
                    data.timeSpent = timeInSeconds
                    tvTimer.text = getFormattedStopWatch(data.timeSpent)
                    db.collection("Tasks").document(data.id).set(data)

                }

            }
        }

    }


    interface ClickListener{
        fun onDelete(task:Task)
        fun openTask(task:Task)
        fun updateTime(totalTimeFormat : String)

    }

    override fun getItemCount(): Int {
        return list.size
    }

}
