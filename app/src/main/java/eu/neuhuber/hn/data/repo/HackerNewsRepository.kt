package eu.neuhuber.hn.data.repo

import android.util.Log
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*


object HackerNewsRepository : NewsRepository {
    private val client = HttpClient(Android) {
        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            url { protocol = URLProtocol.HTTPS }
            host = "hacker-news.firebaseio.com"
        }
        install(JsonFeature)
        install(Logging)
        install(UserAgent) {
            agent = "ktor - eu.neuhuber.hn"
        }
    }

    private suspend inline fun <reified T> HttpClient.tryGet(path: String): Result<T> = try {
        Log.d("repo", "get request to $path")
        Result.success(get(path = path))
    } catch (e: Throwable) {
        Log.e("HNRepo", e.message!!)
        Result.failure(e)
    }

    suspend fun getTopStories(): Result<List<Id>> = client.tryGet("v0/topstories.json")

    override suspend fun getItem(itemId: Id): Result<Item> = client.tryGet("v0/item/$itemId.json")

    suspend fun getNewStories(): Result<List<Id>> {
        return client.tryGet("/v0/newstories.json")
    }

    suspend fun getBestStories(): Result<List<Id>> {
        return client.tryGet("/v0/beststories.json")
    }
}