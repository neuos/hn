package eu.neuhuber.hn.ui.comments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.neuhuber.hn.data.LazyLoader
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.data.repo.HackerNewsRepository
import eu.neuhuber.hn.data.repo.NewsRepository
import eu.neuhuber.hn.ui.util.Refresher

class CommentsViewModel : ViewModel() {
    private val newsRepository: NewsRepository = HackerNewsRepository
    private var errorMessage: String? = null

    fun loadComment(id: Id): LazyCommentTree? = loader.loadValue(id)

    val refresh = Refresher<Id>(viewModelScope) {
        loader.clear()
        loader.loadValue(it)
    }

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
            value?.kids?.let { kids ->
                children = kids.map { LazyCommentTree(it) }
            }
        }
    var children: List<LazyCommentTree> = listOf()
}
