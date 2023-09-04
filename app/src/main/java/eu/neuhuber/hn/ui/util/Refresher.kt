package eu.neuhuber.hn.ui.util

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore

class Refresher<T>(private val scope: CoroutineScope, private val block: suspend (T) -> Unit) {
    private val sem = Semaphore(1)
    private val refreshingState = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = refreshingState.asStateFlow()
    private val logger = Logger.withTag("Refresher")

    operator fun invoke(args: T) {
        logger.d( "refresh request")
        if (sem.tryAcquire()) {
            logger.d("currently not refreshing")
            scope.launch {
                refreshingState.emit(true)
                logger.d( "refreshing started")
                block(args)
                refreshingState.emit(false)
                logger.d( "refreshing done")
                sem.release()
            }
        }
        else logger.d( "already refreshing")
    }
}

operator fun Refresher<Unit>.invoke() = invoke(Unit)