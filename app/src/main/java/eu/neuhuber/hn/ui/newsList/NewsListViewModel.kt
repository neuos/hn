package eu.neuhuber.hn.ui.newsList

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.neuhuber.hn.data.LazyLoader
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.data.repo.BookmarkRepository
import eu.neuhuber.hn.data.repo.NewsRepository
import eu.neuhuber.hn.ui.util.Refresher
import eu.neuhuber.hn.ui.util.invoke
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

sealed class NewsListViewModel(
    protected val newsRepository: NewsRepository,
    protected val bookmarkRepository: BookmarkRepository
) : ViewModel() {
    var storyIds by mutableStateOf<ImmutableList<Id>?>(null)
    var bookmarkedIds by mutableStateOf<ImmutableList<Id>>(persistentListOf())
    val listState by mutableStateOf(LazyListState())

    var errorMessage: String? = null

    val refresh = Refresher<Unit>(viewModelScope) {
        loadIds()
        loadBookmarks()
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
        storyIds = null
        loader.clear()

        val ids: Result<List<Id>> = loadStoryIds()

        ids.onSuccess {
            storyIds = it.toImmutableList()
        }.onFailure {
            errorMessage = it.message
        }
    }

    private suspend fun loadBookmarks() {
        bookmarkedIds =
            bookmarkRepository.getBookmarks().getOrDefault(emptyList()).toImmutableList()
    }

    abstract suspend fun loadStoryIds(): Result<List<Id>>
    open suspend fun toggleBookmark(item: Item): Result<Boolean> {
        val isBookmarked =
            bookmarkRepository.toggleBookmark(item.id).getOrElse { return Result.failure(it) }
        bookmarkedIds = bookmarkedIds.let {
            if (isBookmarked && !it.contains(item.id)) it + item.id
            else if(!isBookmarked && it.contains(item.id)) it - item.id
            else it
        }.toImmutableList()
        return Result.success(isBookmarked)
    }
}

class TopNewsListViewModel(
    newsRepository: NewsRepository, bookmarkRepository: BookmarkRepository
) : NewsListViewModel(newsRepository, bookmarkRepository) {
    override suspend fun loadStoryIds(): Result<List<Id>> = newsRepository.getTopStories()
}

class NewNewsListViewModel(
    newsRepository: NewsRepository, bookmarkRepository: BookmarkRepository
) : NewsListViewModel(newsRepository, bookmarkRepository) {
    override suspend fun loadStoryIds(): Result<List<Id>> = newsRepository.getNewStories()
}

class BestNewsListViewModel(
    newsRepository: NewsRepository, bookmarkRepository: BookmarkRepository
) : NewsListViewModel(newsRepository, bookmarkRepository) {
    override suspend fun loadStoryIds(): Result<List<Id>> = newsRepository.getBestStories()
}

class BookmarksNewsListViewModel(
    newsRepository: NewsRepository, bookmarkRepository: BookmarkRepository
) : NewsListViewModel(newsRepository, bookmarkRepository) {
    override suspend fun loadStoryIds(): Result<List<Id>> {
        val bookmarks = bookmarkRepository.getBookmarks().getOrElse { return Result.failure(it) }
        if(bookmarks.isEmpty()) {
            errorMessage = "Add Bookmarks by swiping on a story"
        }
        return Result.success(bookmarks)
    }

    override suspend fun toggleBookmark(item: Item): Result<Boolean> {
        val isBookmarked = super.toggleBookmark(item).getOrElse { return Result.failure(it) }

        val ids = loadStoryIds().getOrDefault(emptyList())
        storyIds = ids.toImmutableList()

        return Result.success(isBookmarked)
    }
}

