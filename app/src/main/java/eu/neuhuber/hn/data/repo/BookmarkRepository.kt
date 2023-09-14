package eu.neuhuber.hn.data.repo

import eu.neuhuber.hn.data.model.Id

interface BookmarkRepository {
    suspend fun getBookmarks(): Result<List<Id>>
    suspend fun toggleBookmark(id: Id): Result<Boolean>
    suspend fun isBookmarked(id: Id): Result<Boolean>
}