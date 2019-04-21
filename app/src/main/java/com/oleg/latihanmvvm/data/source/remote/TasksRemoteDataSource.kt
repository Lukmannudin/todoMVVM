package com.oleg.latihanmvvm.data.source.remote

import android.os.Handler
import com.google.common.collect.Lists
import com.oleg.latihanmvvm.data.Task
import com.oleg.latihanmvvm.data.source.TasksDataSource

object TasksRemoteDataSource : TasksDataSource {

    private const val SERVICE_LATENCY_IN_MILIS = 5000L

    private var TASKS_SERVICE_DATA = LinkedHashMap<String,Task>(2)

    init {

    }

    private fun addTask(title:String,description:String){
        val newTask = Task(title,description)
        TASKS_SERVICE_DATA.put(newTask.id, newTask)
    }


    /*
    * Note: [LoadTasksCallback.onDataNotAvailable] is never fired. In a real remote data
    * source implementation, this would be fired if the server can't be contacted or the server
    * returns an error
    * */
    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        // Simulate network by delaying the execution
        val tasks = Lists.newArrayList(TASKS_SERVICE_DATA.values)
        Handler().postDelayed({
            callback.onTasksLoaded(tasks)
        }, SERVICE_LATENCY_IN_MILIS)
    }


    /**
     * Note: [GetTaskCallback.onDataNotAvailable] is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        val task = TASKS_SERVICE_DATA[taskId]

        // Simulate network by delaying the execution
        with(Handler()){
            if (task != null){
                postDelayed({callback.onTaskLoaded(task)}, SERVICE_LATENCY_IN_MILIS)
            } else {
                postDelayed({callback.onDataNotAvailable()}, SERVICE_LATENCY_IN_MILIS)
            }
        }
    }

    override fun saveTask(task: Task) {
        TASKS_SERVICE_DATA.put(task.id,task)
    }

    override fun completeTask(task: Task) {
        val completedTask = Task(task.title, task.description, task.id).apply {
            isCompleted = true
        }

        TASKS_SERVICE_DATA.put(task.id,completedTask)
    }

    override fun completeTask(taskId: String) {
        // Not required for the remote data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun activateTask(task: Task) {
        val activeTask = Task(task.title,task.description,task.id)
        TASKS_SERVICE_DATA.put(task.id,activeTask)
    }

    override fun activateTask(taskId: String) {
        // Not required for the remote data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun clearCompletedTasks() {
        TASKS_SERVICE_DATA = TASKS_SERVICE_DATA.filterValues {
            !it.isCompleted
        } as LinkedHashMap<String, Task>
    }

    override fun refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override fun deleteAllTasks() {
        TASKS_SERVICE_DATA.clear()
    }

    override fun deleteTask(taskId: String) {
        TASKS_SERVICE_DATA.remove(taskId)
    }

}