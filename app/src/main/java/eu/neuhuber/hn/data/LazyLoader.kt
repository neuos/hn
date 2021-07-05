package eu.neuhuber.hn.data

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit


class LazyLoader<K, V>(
    scope: CoroutineScope,
    private val load: suspend (K) -> Result<V>
) {
    private val sem = Semaphore(1);
    private val queue = hashSetOf<K>()
    private val map = mutableStateMapOf<K, V>()
    private val loaderScope : CoroutineScope = CoroutineScope(scope.coroutineContext)

    fun loadValue(k: K): V? {
        // already cached
        map[k]?.let { return it }

        Log.v("lazy", "try loading $k")

        loaderScope.launch {
            // possibly currently something loading
            sem.withPermit {
                if (queue.contains(k)) return@launch
                queue.add(k)
            }
            Log.v("lazy", "loading item $k")

            load(k).onSuccess {
                map[k] = it
                Log.v("lazy", "loading done $k")
            }.onFailure {
                Log.w("lazy", "loading failed ${it.message}")
            }
        }
        return map[k]
    }

    suspend fun clear() = sem.withPermit {
        map.clear()
        queue.clear()
    }
}

