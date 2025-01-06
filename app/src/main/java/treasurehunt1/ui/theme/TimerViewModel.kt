/*Joseph Di Lullo
OSU
CS 492
dilulloj@oregonstate.edu
*/

package com.example.treasurehunt1.ui.theme

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel : ViewModel() {
    private val _secondsElapsed = MutableStateFlow(0)
    val secondsElapsed: StateFlow<Int> = _secondsElapsed.asStateFlow()

    private var job: Job? = null

    fun setInitialTime(seconds: Int) {
        _secondsElapsed.value = seconds
        resumeTimer()
    }

    fun startTimer() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(1000)
                _secondsElapsed.value++
            }
        }
    }

    fun pauseTimer() {
        job?.cancel()
    }

    fun resumeTimer() {
        if (job?.isActive != true) {
            startTimer()
        }
    }

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }
}