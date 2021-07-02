package eu.neuhuber.hn.data.repo

import eu.neuhuber.hn.data.model.CommentTree
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item

interface NewsRepository {
    suspend fun getTopStories() : Result<List<Item>>
    suspend fun getComments(itemId: Id): Result<List<CommentTree>>
    suspend fun getItem(itemId: Id):Result<Item>
}
