package com.oleg.latihanmvvm.data.source.local

import androidx.room.*
import com.oleg.latihanmvvm.data.Task

/*
* Data access Object for the tasks table.
* */
@Dao interface TasksDao {

    /*
    * Select all tasks from the tasks table.
    *
    * @return all tasks.
    * */
    @Query("SELECT * FROM tasks") fun getTasks():List<Task>

    /*
    * Select a task by id.
    *
    * @param taskId the task id.
    * @return the task with taskId
    * */
    @Query("SELECT * FROM tasks WHERE entryid = :taskId") fun getTaskById(taskId:String):Task?

    /*
    * Insert a task in the database. If the task already exist, replace it.
    *
    * @param task the task to be inserted
    * */
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertTask(task:Task)

    /*
    * Update a task.
    *
    * @param task to be updated
    * @return the number of task updated. This should always be 1
    *
    * */
    @Update fun updateTask(task:Task):Int

    /*
    * Update the complete status of task
    *
    * @param taskId, id of the task
    * @param completed status to be updated
    * */
    @Query("UPDATE tasks SET completed = :completed WHERE entryid = :taskid")
    fun updateCompleted(taskid: String,completed:Boolean)

    /**
     * Delete a task by id.
     *
     * @return the number of tasks deleted. This should always be 1.
     */
    @Query("DELETE FROM Tasks WHERE entryid = :taskId") fun deleteTaskById(taskId: String): Int

    /**
     * Delete all tasks.
     */
    @Query("DELETE FROM Tasks") fun deleteTasks()

    /**
     * Delete all completed tasks from the table.
     *
     * @return the number of tasks deleted.
     */
    @Query("DELETE FROM Tasks WHERE completed = 1") fun deleteCompletedTasks(): Int

}