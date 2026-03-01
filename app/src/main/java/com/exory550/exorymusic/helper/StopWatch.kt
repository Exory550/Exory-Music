package com.exory550.exorymusic.helper

class StopWatch {

    private var startTime: Long = 0

    private var previousElapsedTime: Long = 0

    private var isRunning: Boolean = false

    val elapsedTime: Long
        get() = synchronized(this) {
            var currentElapsedTime: Long = 0
            if (isRunning) {
                currentElapsedTime = System.currentTimeMillis() - startTime
            }
            return previousElapsedTime + currentElapsedTime
        }

    fun start() {
        synchronized(this) {
            startTime = System.currentTimeMillis()
            isRunning = true
        }
    }

    fun pause() {
        synchronized(this) {
            previousElapsedTime += System.currentTimeMillis() - startTime
            isRunning = false
        }
    }

    fun reset() {
        synchronized(this) {
            startTime = 0
            previousElapsedTime = 0
            isRunning = false
        }
    }

    override fun toString(): String {
        return String.format("%d millis", elapsedTime)
    }
}
