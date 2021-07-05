package eu.neuhuber.hn.data.repo

import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item

interface NewsRepository {
    suspend fun getItem(itemId: Id):Result<Item>
}
