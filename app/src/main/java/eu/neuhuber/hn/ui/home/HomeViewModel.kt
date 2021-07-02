package eu.neuhuber.hn.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.neuhuber.hn.data.LazyLoader
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.data.repo.HackerNewsRepository
import eu.neuhuber.hn.ui.util.Refresher
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val newsRepository: HackerNewsRepository = HackerNewsRepository()
    val storyIds = mutableStateOf<List<Id>?>(null)

    var errorMessage: String? = null

    val refresher = Refresher(viewModelScope) {
        loadIds()
    }

    private val loader = LazyLoader(viewModelScope) { id: Id ->
        newsRepository.getItem(id)
    }

    fun loadStory(id: Id): Item? = loader.loadValue(id)

    init {
        viewModelScope.launch {
            refresher.refresh()
        }
    }

    private suspend fun loadIds() {
        storyIds.value = null
        loader.clear()
        newsRepository.getTopStoryIds().onSuccess { ids ->
            storyIds.value = ids
        }.onFailure {
            errorMessage = it.message
        }
    }
}


