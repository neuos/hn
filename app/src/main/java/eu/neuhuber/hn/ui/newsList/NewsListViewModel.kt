package eu.neuhuber.hn.ui.newsList

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.neuhuber.hn.data.LazyLoader
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.data.repo.HackerNewsRepository
import eu.neuhuber.hn.data.repo.NewsRepository
import eu.neuhuber.hn.ui.util.Refresher
import eu.neuhuber.hn.ui.util.invoke

sealed class NewsListViewModel : ViewModel() {
    protected val newsRepository: NewsRepository = HackerNewsRepository
    val storyIds = mutableStateOf<List<Id>?>(null)
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
            storyIds.value = it
        }.onFailure {
            errorMessage = it.message
        }
    }

    abstract suspend fun loadStoryIds(): Result<List<Id>>
}

class TopNewsListViewModel : NewsListViewModel() {
    override suspend fun loadStoryIds(): Result<List<Id>> = newsRepository.getTopStories()
}

class NewNewsListViewModel : NewsListViewModel() {
    override suspend fun loadStoryIds(): Result<List<Id>> = newsRepository.getNewStories()
}

class BestNewsListViewModel : NewsListViewModel() {
    override suspend fun loadStoryIds(): Result<List<Id>> = newsRepository.getBestStories()
}