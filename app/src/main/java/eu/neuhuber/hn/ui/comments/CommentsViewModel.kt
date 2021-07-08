package eu.neuhuber.hn.ui.comments

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.neuhuber.hn.data.LazyLoader
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.data.repo.HackerNewsRepository
import eu.neuhuber.hn.data.repo.NewsRepository
import kotlinx.coroutines.launch
import java.util.concurrent.Semaphore

class CommentsViewModel : ViewModel() {
    private val newsRepository: NewsRepository = HackerNewsRepository
    var errorMessage: String? = null

    fun loadComment(id:Id): LazyCommentTree? = loader.loadValue(id)

    private val loader = LazyLoader<Id, LazyCommentTree>(viewModelScope) { id ->
        loadLazyCommentTree(id)
    }

    private suspend fun loadLazyCommentTree(id: Id): Result<LazyCommentTree> {
        val tree = LazyCommentTree(id)
        val item = newsRepository.getItem(id)
        item.onFailure {
            errorMessage = it.message
            Log.e(javaClass.name, it.message.toString())
            Log.e(javaClass.name, it.stackTraceToString())
            return Result.failure(it)
        }.onSuccess {
            tree.item = it
            return Result.success(tree)
        }
        return Result.failure(Exception())
    }

}

class LazyCommentTree(val id: Id) {
    var item: Item? = null
        set(value) {
            field = value
            value?.kids?.let {
                children = it.map { LazyCommentTree(it) }
            }
        }
    var children: List<LazyCommentTree> = listOf()
}
