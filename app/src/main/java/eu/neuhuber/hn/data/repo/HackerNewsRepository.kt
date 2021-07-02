package eu.neuhuber.hn.data.repo

import android.util.Log
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.ui.comments.CommentTree
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*


class HackerNewsRepository : NewsRepository {

    private val client = HttpClient(Android) {
        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            url { protocol = URLProtocol.HTTPS }
            host = "hacker-news.firebaseio.com"
        }
        install(JsonFeature)
        install(Logging)

    }

    suspend fun getTopStoryIds():Result<List<Id>> = client.tryGet("v0/topstories.json")

    override suspend fun getTopStories(): Result<List<Item>> {
        getTopStoryIds().onSuccess {
            val items: List<Item> = it.take(16).mapNotNull { id ->
                getItem(id).getOrNull()
            }
            return Result.success(items)
        }.onFailure {
            return Result.failure(it)
        }
        return Result.failure(Exception())
    }

    override suspend fun getComments(itemId: Id): Result<List<CommentTree>> {
        getItem(itemId).onSuccess {
            return Result.success(getComments(it))
        }.onFailure {
            return Result.failure(it)
        }
        return Result.failure(Exception())
    }

    private val initalDepth = Int.MAX_VALUE
    private val initalWidth = 6//Int.MAX_VALUE

    private suspend fun getComments(
        item: Item,
        depth: Int = initalDepth,
        width: Int = initalWidth
    ): List<CommentTree> {
        return item.kids?.take(width)
            ?.mapNotNull { id -> getItem(id).getOrNull() }
            ?.map { CommentTree(it, getComments(it, depth - 1, width)) }
            ?: emptyList()
    }

    suspend fun getNewStories(): Result<List<Item>> = client.tryGet("v0/newstories.json")

    override suspend fun getItem(itemId: Id): Result<Item> = client.tryGet("v0/item/$itemId.json")


    private suspend inline fun <reified T> HttpClient.tryGet(path: String): Result<T> = try {
        Log.i("repo", "get request to $path")
        Result.success(get(path = path))
    } catch (e: Throwable) {
        Log.e("HNRepo", e.message!!)
        Result.failure(e)
    }

}