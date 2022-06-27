package eu.neuhuber.hn.data.repo

import android.util.Log
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

object HackerNewsRepository : NewsRepository {
    private val client = HttpClient(Android) {
        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            url { protocol = URLProtocol.HTTPS }
            host = "hacker-news.firebaseio.com"
        }
        install(ContentNegotiation) {
            json()
        }
        install(Logging)
        install(UserAgent) {
            agent = "ktor - eu.neuhuber.hn"
        }
    }

    private suspend inline fun <reified T> HttpClient.tryGet(path: String): Result<T> = try {
        Log.d(javaClass.name, "get request to $path")
        Result.success(get(urlString = path).body())
    } catch (e: Throwable) {
        Log.e(javaClass.name, e.message!!)
        Result.failure(e)
    }

    override suspend fun getTopStories(): Result<List<Id>> = client.tryGet("v0/topstories.json")

    override suspend fun getItem(itemId: Id): Result<Item> = client.tryGet("v0/item/$itemId.json")

    override suspend fun getNewStories(): Result<List<Id>> = client.tryGet("/v0/newstories.json")

    override suspend fun getBestStories(): Result<List<Id>> = client.tryGet("/v0/beststories.json")
}