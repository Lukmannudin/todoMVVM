package com.oleg.latihanmvvm.util


/*
* Contains a static reference to [IdLingResource], only available in the 'mock' build type.
* */
object EspressoIdlingResource {
    private val RESOURCE = "GLOBAL"

    @JvmField val countingIdlingResource = SimpleCountingIdlingResource(RESOURCE)

    fun increment(){
        countingIdlingResource.increment()
    }

    fun decrement(){
        countingIdlingResource.decrement()
    }
}