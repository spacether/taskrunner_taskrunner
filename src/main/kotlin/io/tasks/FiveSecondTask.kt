package io.tasks

import kotlinx.coroutines.Runnable

class FiveSecondTask: Runnable {
    private val fiveSecondsInMilliSeconds = 5000L

    override fun run() {
        Thread.sleep(fiveSecondsInMilliSeconds)
    }
}