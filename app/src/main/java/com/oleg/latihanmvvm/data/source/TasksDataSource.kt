package com.oleg.latihanmvvm.data.source

import com.oleg.latihanmvvm.data.Task

interface   TasksDataSource {
    interface LoadTasksCallback {

        fun onTasksLoaded(tasks:List<Task>)

        fun onDataNotAvailable()
    }

    interface GetTaskCallback {

        fun onTaskLoaded(task:Task)

        fun onDataNotAvailable()
    }

    fun getTasks(callback: LoadTasksCallback)

    fun getTask(taskId:String, callback:GetTaskCallback)

    fun saveTask(task: Task)

    fun completeTask(task: Task)

    fun completeTask(taskId: String)

    fun activeTask(task: Task)

    fun activeTask(taskId:String)

    fun clearCompletedTasks()

    fun refreshTasks()

    fun deleteAllTasks()

    fun deleteTask(taskId:String)
}