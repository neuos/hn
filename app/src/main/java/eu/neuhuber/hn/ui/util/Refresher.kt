package eu.neuhuber.hn.ui.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore

class Refresher(private val scope: CoroutineScope, private val block: suspend () -> Unit) {
    private val sem = Semaphore(1)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    fun refresh() {
        Log.i("refresher", "refresh request")
        if (sem.tryAcquire()) {
            Log.i("refresher", "currently not refreshing")
            scope.launch {
                _isRefreshing.emit(true)
                Log.i("refresher", "refreshing started")
                block()
                _isRefreshing.emit(false)
                Log.i("refresher", "refreshing done")
                sem.release()
            }
        }
    }
}