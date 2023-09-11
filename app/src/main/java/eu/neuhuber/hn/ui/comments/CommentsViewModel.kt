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
import eu.neuhuber.hn.data.repo.FakeNewsRepository
import eu.neuhuber.hn.data.repo.NewsRepository
import eu.neuhuber.hn.ui.util.Refresher

class CommentsViewModel : ViewModel() {
    private val newsRepository: NewsRepository = FakeNewsRepository
    private val logger = Logger.withTag("CommentsViewModel")
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadComment(id: Id): LazyCommentTree? = loader.loadValue(id)

    val refreshAll = Refresher<Id>(viewModelScope) {
        loader.clear()
        loader.loadValue(it)
    }

    val refreshSingle = Refresher<Id>(viewModelScope) {
        loader.clear(it)
        loader.loadValue(it)
    }

    private val loader = LazyLoader<Id, LazyCommentTree>(viewModelScope) { id ->
        loadLazyCommentTree(id)
    }

    // TODO: single item error should show on the single element
    private suspend fun loadLazyCommentTree(id: Id): Result<LazyCommentTree> {
        errorMessage = null
        val item = newsRepository.getItem(id)
        return item.fold(
            onSuccess = {
                val tree = LazyCommentTree(id)
                tree.node = LazyCommentNode.Comment(it)
                Result.success(tree)
            },
            onFailure = {
                logger.e(it) { "failed to load item $id" }
                Result.failure(it)
            }
        )
    }

}

class LazyCommentTree(val id: Id) {
    var node: LazyCommentNode = LazyCommentNode.Loading
        set(value) {
            field = value
            if (value is LazyCommentNode.Comment) {
                children = value.item.kids?.map { LazyCommentTree(it) } ?: listOf()
            }
        }
    var children: List<LazyCommentTree> = listOf()
}

sealed class LazyCommentNode {
    data object Loading : LazyCommentNode()
    data class Comment(val item: Item) : LazyCommentNode()
    data class Error(val message: String) : LazyCommentNode()
}