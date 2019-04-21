package com.oleg.latihanmvvm.util

import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import android.os.Handler

const val THREAD_COUNT = 3

/*
* Global executors pools for the whole application.
*
* Grouping tasks like this avoids the effects of task starvation (e.g. disks reads don't wait behind)
* webservice requests).
* */

open class AppExecutors constructor(
    val diskIO: Executor = DiskIOThreadExecutor(),
    val networkIO: Executor = Executors.newFixedThreadPool(THREAD_COUNT),
    val mainThread: Executor = MainThreadExecutor()
){
    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable?) {
            mainThreadHandler.post(command)
        }
    }
}