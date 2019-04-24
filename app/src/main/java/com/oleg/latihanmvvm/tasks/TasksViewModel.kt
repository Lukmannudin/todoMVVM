package com.oleg.latihanmvvm.tasks

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.oleg.latihanmvvm.Event
import com.oleg.latihanmvvm.R
import com.oleg.latihanmvvm.data.Task
import com.oleg.latihanmvvm.data.source.TasksDataSource
import com.oleg.latihanmvvm.data.source.TasksRepository

class TasksViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<Task>>().apply {
        value = emptyList()
    }

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean>
        get() = _dataLoading

    private val _currentFilteringLabel = MutableLiveData<Int>()
    val currentFilteringLabel: LiveData<Int>
        get() = _currentFilteringLabel

    private val _noTasksLabel = MutableLiveData<Int>()
    val noTasksLabel: LiveData<Int>
        get() = _noTasksLabel

    private val _noTaskIconRes = MutableLiveData<Int>()
    val noTaskIconRes: LiveData<Int>
        get() = _noTaskIconRes

    private val _tasksAddViewVisible = MutableLiveData<Boolean>()
    val tasksAddViewVisible: LiveData<Boolean>
        get() = _tasksAddViewVisible

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>>
        get() = _snackbarText

    private var _currentFiltering = TasksFilterType.ALL_TASKS

    // Not used at the moment
    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _openTaskEvent = MutableLiveData<Event<String>>()
    val openTaskEvent: LiveData<Event<String>>
        get() = _openTaskEvent

    private val _newTaskEvent = MutableLiveData<Event<Unit>>()
    val newTaskEvent: LiveData<Event<Unit>>
        get() = _newTaskEvent


    // This LiveData depends on another so we can use a transformation
    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    init {
        // Set initial state
    }

    fun start() {

    }

    fun loadTasks(forceUpdate: Boolean) {

    }

    /*
    * Sets the current task filtering type.
    *
    * @param requestType Can be [TaskFilterType.ALL_TASKS],
    * [TaskFilterType.COMPLETED_TASKS], or
    * [TaskFilterType.ACTIVE_TASKS]
    * */
//    fun setFiltering(requestType: TasksFilterType) {
//        _currentFiltering = requestType
//
//        // Depending on the filter type, set the filtering label, icon drawables, etc.
//        when (requestType) {
//            TasksFilterType.ALL_TASKS -> {
////            setF
//            }
//        }
//    }

    private fun setFilter(@StringRes filteringLabelString: Int, @StringRes noTasksLabelString:Int,
                          @DrawableRes noTaskIconDrawable: Int, taskAddVisible: Boolean){
        _currentFilteringLabel.value = filteringLabelString
        _noTasksLabel.value = noTasksLabelString
        _noTaskIconRes.value = noTaskIconDrawable
        _tasksAddViewVisible.value = taskAddVisible
    }

    fun clearCompletedTasks(){
        tasksRepository.clearCompletedTasks()
        _snackbarText.value = Event(R.string.completed_tasks_cleared)
        loadTasks(false,false)
    }

    fun completeTask(task:Task, completed: Boolean){
        // Notify repository
        if (completed){
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }


    /*
    * Called by the Data Binding library and the FAB's click listener
    * */
    fun addNewTask(){
        _newTaskEvent.value = Event(Unit)
    }

    /*
    * Called by the [TasksAdapter].
    * */
    internal fun openTask(taskId: String){
        _openTaskEvent.value = Event(taskId)
    }

//    fun handleActivityResult(requestCode:Int, resultCode:Int){
//        if (AddEdit)
//    }

    private fun showSnackbarMessage(message:Int){
        _snackbarText.value = Event(message)
    }

    /*
    * @param forceUpdate    Pass in true to refresh the data in the [TasksDataSource]
    * @param showLoadingUI  Pass in true to display a loading icon in the UI
    * */
    private fun loadTasks(forceUpdate: Boolean, showLoadingUI: Boolean){
        if (showLoadingUI){
            _dataLoading.value = true
        }

        if (forceUpdate){
            tasksRepository.refreshTasks()
        }

        tasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback{
            override fun onTasksLoaded(tasks: List<Task>) {
                val tasksToShow = ArrayList<Task>()

                // we filter the tasks base on the request type
                for (task in tasks){
                    when (_currentFiltering){
                        TasksFilterType.ALL_TASKS -> tasksToShow.add(task)
                        TasksFilterType.ACTIVE_TASKS -> if (task.isActive){
                            tasksToShow.add(task)
                        }
                        TasksFilterType.COMPLETED_TASKS -> if (task.isCompleted){
                            tasksToShow.add(task)
                        }
                    }
                }
                if (showLoadingUI){
                    _dataLoading.value = false
                }
                isDataLoadingError.value = false

                val itemsValue = ArrayList(tasksToShow)
                _items.value = itemsValue
            }

            override fun onDataNotAvailable() {
                isDataLoadingError.value = true
            }
        })
    }


}