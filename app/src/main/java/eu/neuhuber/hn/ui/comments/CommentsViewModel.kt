package eu.neuhuber.hn.ui.comments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import eu.neuhuber.hn.data.LazyLoader
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.data.repo.HackerNewsRepository
import eu.neuhuber.hn.data.repo.NewsRepository
import eu.neuhuber.hn.ui.util.Refresher

class CommentsViewModel : ViewModel() {
    private val newsRepository: NewsRepository = HackerNewsRepository
    private val logger = Logger.withTag("CommentsViewModel")
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadComment(id: Id): LazyCommentTree? = loader.loadValue(id)

    val refresh = Refresher<Id>(viewModelScope) {
        loader.clear()
        loader.loadValue(it)
    }

    private val loader = LazyLoader<Id, LazyCommentTree>(viewModelScope) { id ->
        loadLazyCommentTree(id)
    }

    // TODO: single item error should show on the single element
    private suspend fun loadLazyCommentTree(id: Id): Result<LazyCommentTree> {
        errorMessage = null
        val tree = LazyCommentTree(id)
        val item = newsRepository.getItem(id)
        return item.fold(
            onSuccess = {
                tree.item = it
                Result.success(tree)
            },
            onFailure = {
                errorMessage = it.message
                logger.e(it) { "failed to load item $id" }
                Result.failure(it)
            }
        )
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
