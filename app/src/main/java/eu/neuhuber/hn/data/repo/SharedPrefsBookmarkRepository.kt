package eu.neuhuber.hn.data.repo

import android.content.Context
import android.content.Context.MODE_PRIVATE
import eu.neuhuber.hn.data.model.Id

class SharedPrefsBookmarkRepository(context: Context) : BookmarkRepository {
    private val prefs = context.getSharedPreferences("bookmarks", MODE_PRIVATE)
    override suspend fun getBookmarks(): Result<List<Id>> =
        Result.success(prefs.all.filter { it.value == true }.keys.map { it.toLong() })

    private fun addBookmark(id: Id) = prefs.edit().putBoolean(id.toString(), true).apply()
    private fun removeBookmark(id: Id) = prefs.edit().remove(id.toString()).apply()
    override suspend fun toggleBookmark(id: Id): Result<Boolean> {
        return if (prefs.contains(id.toString())) {
            removeBookmark(id)
            Result.success(false)
        } else {
            addBookmark(id)
            Result.success(true)
        }
    }

}