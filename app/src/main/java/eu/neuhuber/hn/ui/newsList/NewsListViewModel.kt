package eu.neuhuber.hn.ui.newsList

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.neuhuber.hn.data.LazyLoader
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item

import eu.neuhuber.hn.data.repo.NewsRepository
import eu.neuhuber.hn.ui.util.Refresher
import eu.neuhuber.hn.ui.util.invoke
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

sealed class NewsListViewModel(protected val newsRepository: NewsRepository) : ViewModel() {
    val storyIds = mutableStateOf<ImmutableList<Id>?>(null)
    val listState = mutableStateOf(LazyListState())

    var errorMessage: String? = null

    val refresh = Refresher<Unit>(viewModelScope) {
        loadIds()
    }

    private val loader = LazyLoader(viewModelScope) { id: Id ->
        newsRepository.getItem(id)
    }

    fun loadStory(id: Id): Item? = loader.loadValue(id)

    init {
        refresh()
    }

    private suspend fun loadIds() {
        errorMessage = null
        storyIds.value = null
        loader.clear()

        val ids: Result<List<Id>> = loadStoryIds()

        ids.onSuccess {
            storyIds.value = it.toImmutableList()
        }.onFailure {
            errorMessage = it.message
        }
    }

    abstract suspend fun loadStoryIds(): Result<List<Id>>
}

class TopNewsListViewModel(newsRepository: NewsRepository) : NewsListViewModel(newsRepository) {
    override suspend fun loadStoryIds(): Result<List<Id>> = newsRepository.getTopStories()
}

class NewNewsListViewModel(newsRepository: NewsRepository) : NewsListViewModel(newsRepository) {
    override suspend fun loadStoryIds(): Result<List<Id>> = newsRepository.getNewStories()
}

class BestNewsListViewModel(newsRepository: NewsRepository) : NewsListViewModel(newsRepository) {
    override suspend fun loadStoryIds(): Result<List<Id>> = newsRepository.getBestStories()
}