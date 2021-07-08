package eu.neuhuber.hn.data.repo

import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item

interface NewsRepository {
    suspend fun getItem(itemId: Id): Result<Item>
    suspend fun getTopStories(): Result<List<Id>>
    suspend fun getNewStories(): Result<List<Id>>
    suspend fun getBestStories(): Result<List<Id>>
}


//TODO: DI