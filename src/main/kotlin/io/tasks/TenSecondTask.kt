package io.tasks

import kotlinx.coroutines.Runnable

class TenSecondTask: Runnable {
    private val tenSecondsInMilliSeconds = 10000L

    override fun run() {
        Thread.sleep(tenSecondsInMilliSeconds)
    }
}