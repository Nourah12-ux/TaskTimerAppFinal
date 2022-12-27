package com.example.timertaskapp





data class Task(var id:String,val taskName:String, val taskDetails:String, var timeSpent:Long, var flag:Boolean):java.io.Serializable{
}
