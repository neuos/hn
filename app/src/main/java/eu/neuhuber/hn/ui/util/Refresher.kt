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
    private val refreshingState = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = refreshingState.asStateFlow()

    operator fun invoke() {
        Log.i(javaClass.name, "refresh request")
        if (sem.tryAcquire()) {
            Log.i(javaClass.name, "currently not refreshing")
            scope.launch {
                refreshingState.emit(true)
                Log.i(javaClass.name, "refreshing started")
                block()
                refreshingState.emit(false)
                Log.i(javaClass.name, "refreshing done")
                sem.release()
            }
        }
    }
}