package eu.neuhuber.hn.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.neuhuber.hn.data.LazyLoader
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.data.repo.HackerNewsRepository
import eu.neuhuber.hn.data.repo.NewsRepository
import eu.neuhuber.hn.ui.util.Refresher
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val newsRepository: NewsRepository = HackerNewsRepository
    val storyIds = mutableStateOf<List<Id>?>(null)

    var errorMessage: String? = null

    val refresh = Refresher(viewModelScope) {
        loadIds()
    }

    private val loader = LazyLoader(viewModelScope) { id: Id ->
        newsRepository.getItem(id)
    }

    val selected = mutableStateOf(SelectedList.Top)

    fun loadStory(id: Id): Item? = loader.loadValue(id)

    init {
        refresh()
    }

    fun changeView(it: SelectedList): Boolean {
        if (selected.value == it) return false
        selected.value = it
        viewModelScope.launch {
            loadIds()
        }
        return true
    }

    private suspend fun loadIds() {
        errorMessage = null
        storyIds.value = null
        loader.clear()

        val ids: Result<List<Id>> = when (selected.value) {
            SelectedList.Top -> newsRepository.getTopStories()
            SelectedList.New -> newsRepository.getNewStories()
            SelectedList.Best -> newsRepository.getBestStories()
        }

        ids.onSuccess {
            storyIds.value = it
        }.onFailure {
            errorMessage = it.message
        }
    }
}
