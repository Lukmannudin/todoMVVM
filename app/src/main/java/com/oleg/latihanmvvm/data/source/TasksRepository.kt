package com.oleg.latihanmvvm.data.source

import com.oleg.latihanmvvm.data.Task
import com.oleg.latihanmvvm.util.EspressoIdlingResource

class TasksRepository(
    val tasksRemoteDataSource: TasksDataSource,
    val tasksLocalDataSource: TasksDataSource
): TasksDataSource {

    /*
    * This variable has public visibility so it can be accessed from tests;
    * */
    var cachedTasks: LinkedHashMap<String, Task> = LinkedHashMap()

    /*
     * Marks the cache as invalid, to force an update the next time is requested. This variable
     * has package local visibility so it can be accessed from tests
     * */
    var cacheIsDirty = false

    /*
    * Marks the cache as invalid, to force an update the next time is requested. This variable
    * has package local visibility so it can be accessed from tests
    *
    * Note: [LoadTasksCallback.onDataNotAvailable] is fired if all data sources fail to get data
    * */

    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        // Respond immadiately with cache if available and not dirty
        if (cachedTasks.isNotEmpty() && !cacheIsDirty){
            callback.onTasksLoaded(ArrayList(cachedTasks.values))
            return
        }

        EspressoIdlingResource.increment() // set app as busy

        if (cacheIsDirty){
            // If the cache is dirty we need to fetch new data from network
            getTasksFromRemoteDataSource(callback)
        } else {
            // Query the local data storage if available. If not, query the network
            tasksLocalDataSource.getTasks(object :TasksDataSource.LoadTasksCallback{
                override fun onTasksLoaded(tasks: List<Task>) {
                    refreshCache(tasks)
                    EspressoIdlingResource.decrement() // Set app as idle
                    callback.onTasksLoaded(ArrayList(cachedTasks.values))
                }

                override fun onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback)
                }
            })
        }
    }


    override fun saveTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        cacheAndPerform(task){
            tasksLocalDataSource.saveTask(it)
            tasksRemoteDataSource.saveTask(it)
        }
    }

    override fun completeTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        cacheAndPerform(task){
            it.isCompleted = true
            tasksLocalDataSource.completeTask(it)
            tasksRemoteDataSource.completeTask(it)
        }
    }

    override fun completeTask(taskId: String) {
        getTaskWithId(taskId)?.let {
            completeTask(it)
        }
    }

    override fun activateTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        cacheAndPerform(task){
            it.isCompleted = false
            tasksLocalDataSource.activateTask(it)
            tasksRemoteDataSource.activateTask(it)
        }
    }


    override fun activateTask(taskId: String) {
        getTaskWithId(taskId)?.let {
            activateTask(it)
        }
    }

    override fun clearCompletedTasks() {
        tasksLocalDataSource.clearCompletedTasks()
        tasksRemoteDataSource.clearCompletedTasks()

        cachedTasks = cachedTasks.filterValues {
            !it.isCompleted
        } as LinkedHashMap<String, Task>
    }


    /*
    * Get tasks from local data source (sqlite) unless the table is new or empty. In that case it
    * uses the network data source. This is done to simplify the sample.
    *
    *
    * Note: [GetTaskCallback.onDataNotAvailable] is fired if both data fail to get the data
    * */
    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        val taskInCache = getTaskWithId(taskId)

        // Respond immediately with cache is available
        if (taskInCache != null){
            callback.onTaskLoaded(taskInCache)
            return
        }

        EspressoIdlingResource.increment() // Set App as busy

        // Load from server/persited if needed

        // Is the task in the local data source? If not, query the network
        tasksLocalDataSource.getTask(taskId,object : TasksDataSource.GetTaskCallback{
            override fun onTaskLoaded(task: Task) {
                // Do in memory cache update to keep the app UI up to date
                cacheAndPerform(task){
                    EspressoIdlingResource.decrement()
                    callback.onTaskLoaded(it)
                }
            }

            override fun onDataNotAvailable() {
                tasksRemoteDataSource.getTask(taskId,object :TasksDataSource.GetTaskCallback{
                    override fun onTaskLoaded(task: Task) {
                        // Do in memory cache update to keep the app UI up to date
                        cacheAndPerform(task){
                            EspressoIdlingResource.decrement() // Set app as idle
                            callback.onTaskLoaded(it)
                        }
                    }

                    override fun onDataNotAvailable() {
                        EspressoIdlingResource.decrement() // Set app as idle
                        callback.onDataNotAvailable()
                    }

                })
            }
        })
    }

    override fun refreshTasks() {
        cacheIsDirty = true
    }

    override fun deleteAllTasks() {
        tasksLocalDataSource.deleteAllTasks()
        tasksRemoteDataSource.deleteAllTasks()
        cachedTasks.clear()
    }

    override fun deleteTask(taskId: String) {
        tasksLocalDataSource.deleteTask(taskId)
        tasksRemoteDataSource.deleteTask(taskId)
        cachedTasks.remove(taskId)
    }

    private fun getTasksFromRemoteDataSource(callback: TasksDataSource.LoadTasksCallback){
        tasksRemoteDataSource.getTasks(object :TasksDataSource.LoadTasksCallback{
            override fun onTasksLoaded(tasks: List<Task>) {
                refreshCache(tasks)
                refreshLocalDataSource(tasks)

                EspressoIdlingResource.decrement() // Set app as idle
                callback.onTasksLoaded(ArrayList(cachedTasks.values))
            }

            override fun onDataNotAvailable() {
                EspressoIdlingResource.decrement() // Set app as idle
                callback.onDataNotAvailable()
            }

        })
    }

    private fun refreshCache(tasks: List<Task>){
        cachedTasks.clear()
        tasks.forEach {
            cacheAndPerform(it){}
        }
        cacheIsDirty = false
    }

    private fun refreshLocalDataSource(tasks:List<Task>){
        tasksRemoteDataSource.deleteAllTasks()
        for (task in tasks){
            tasksLocalDataSource.saveTask(task)
        }
    }

    private inline fun cacheAndPerform(task: Task,perform: (Task)-> Unit){
        val cachedTask = Task(task.title,task.description,task.id).apply {
            isCompleted = task.isCompleted
        }

        cachedTasks.put(cachedTask.id,cachedTask)
        perform(cachedTask)
    }

    private fun getTaskWithId(id:String) = cachedTasks[id]

    companion object {
        private var INSTANCE: TasksRepository? = null

        /*
        * Returns the single instance of this class, creating it if necessary
        *
        *@param taskRemoteDataSource the backend data source
        *
        *@param taskLocalDataSource the device storage data source
        *
        *@return the [TasksRepository] instance
        * */
        @JvmStatic fun getInstance(tasksRemoteDataSource: TasksDataSource,
                                   tasksLocalDataSource: TasksDataSource) =
                INSTANCE ?: synchronized(TasksRepository::class.java){
                    INSTANCE ?: TasksRepository(tasksRemoteDataSource,tasksLocalDataSource)
                        .also { INSTANCE = it }
                }

        /*
        * Used to force [getInstance] to create a new instance
        * next time it's called
        * */
        @JvmStatic fun destroyInstance(){
            INSTANCE = null
        }
    }
}