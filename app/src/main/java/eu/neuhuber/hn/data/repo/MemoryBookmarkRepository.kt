package eu.neuhuber.hn.data.repo

import eu.neuhuber.hn.data.model.Id

class MemoryBookmarkRepository : BookmarkRepository {
    private val bookmarks = sortedSetOf<Id>(compareByDescending { it }, 1, 7, 90)
    override suspend fun getBookmarks(): Result<List<Id>> {
        return Result.success(bookmarks.toList())
    }

    override suspend fun toggleBookmark(id: Id): Result<Boolean> {
        return if (bookmarks.contains(id)) {
            bookmarks.remove(id)
            Result.success(false)
        } else {
            bookmarks.add(id)
            Result.success(true)
        }
    }
}