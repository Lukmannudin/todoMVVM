package com.oleg.latihanmvvm.data.source.local

import androidx.annotation.VisibleForTesting
import com.oleg.latihanmvvm.data.Task
import com.oleg.latihanmvvm.data.source.TasksDataSource
import com.oleg.latihanmvvm.util.AppExecutors

class TasksLocalDataSource private constructor(
    val appExecutors: AppExecutors,
    val tasksDao: TasksDao
) : TasksDataSource {

    /*
    * Note: [LoadTasksCallback.onDataNotAvailable] is fired if the database doesn't exist
    * or the the table is empty.
    * */

    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        appExecutors.diskIO.execute {
            val tasks = tasksDao.getTasks()
            appExecutors.mainThread.execute {
                appExecutors.mainThread.execute {
                    if (tasks.isEmpty()){
                        // This will be called if the table is new or just empty
                        callback.onDataNotAvailable()
                    } else {
                        callback.onTasksLoaded(tasks)
                    }
                }
            }
        }
    }

    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        appExecutors.diskIO.execute {
            val task = tasksDao.getTaskById(taskId)
            appExecutors.mainThread.execute {
                if (task != null){
                    callback.onTaskLoaded(task)
                } else {
                    callback.onDataNotAvailable()
                }
            }
        }
    }

    override fun saveTask(task: Task) {
        appExecutors.diskIO.execute {tasksDao.insertTask(task)}
    }

    override fun completeTask(task: Task) {
        appExecutors.diskIO.execute { tasksDao.updateCompleted(task.id,true)}
    }

    override fun completeTask(taskId: String) {
        // Not required for the local data source because the {@link TaskRepository} handles
        // Converting from {@code taskId} to a {@link task} using its cached data.
    }

    override fun activeTask(task: Task) {
        appExecutors.diskIO.execute { tasksDao.updateCompleted(task.id,false)}
    }

    override fun activeTask(taskId: String) {
        // Not required for the local data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun clearCompletedTasks() {
        appExecutors.diskIO.execute { tasksDao.deleteCompletedTasks() }
    }

    override fun refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override fun deleteAllTasks() {
        appExecutors.diskIO.execute { tasksDao.deleteTasks()}
    }

    override fun deleteTask(taskId: String) {
        appExecutors.diskIO.execute{tasksDao.deleteTaskById(taskId)}
    }

    companion object {
        private var INSTANCE: TasksLocalDataSource? = null

        @JvmStatic
        fun getInstance(appExecutors: AppExecutors,tasksDao: TasksDao):TasksLocalDataSource {
            if (INSTANCE == null){
                synchronized(TasksLocalDataSource::javaClass){
                    INSTANCE = TasksLocalDataSource(appExecutors,tasksDao)
                }
            }

            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance(){
            INSTANCE = null
        }
    }
}