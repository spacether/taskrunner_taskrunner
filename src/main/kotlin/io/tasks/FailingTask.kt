package io.tasks

import kotlinx.coroutines.Runnable

class FailingTask: Runnable {
    override fun run() {
        throw RuntimeException("some error in the task")
    }
}