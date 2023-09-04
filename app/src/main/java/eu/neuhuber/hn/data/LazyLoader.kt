package eu.neuhuber.hn.data

import androidx.compose.runtime.mutableStateMapOf
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit


class LazyLoader<K, V>(
    scope: CoroutineScope,
    private val load: suspend (K) -> Result<V>
) {
    private val sem = Semaphore(1)
    private val queue = hashSetOf<K>()
    private val map = mutableStateMapOf<K, V>()
    private val loaderScope: CoroutineScope = CoroutineScope(scope.coroutineContext)
    private val logger = Logger.withTag("LazyLoader")

    fun loadValue(k: K): V? {
        // already cached
        map[k]?.let { return it }

        logger.v { "try loading $k" }

        loaderScope.launch {
            // possibly currently something loading
            sem.withPermit {
                if (queue.contains(k)) return@launch
                queue.add(k)
            }
            logger.v { "loading item $k" }

            load(k).onSuccess {
                map[k] = it
                logger.v { "loading done $k" }
            }.onFailure {
                logger.e(it) { "loading failed" }
            }
        }
        return map[k]
    }

    suspend fun clear() = sem.withPermit {
        map.clear()
        queue.clear()
    }
}

