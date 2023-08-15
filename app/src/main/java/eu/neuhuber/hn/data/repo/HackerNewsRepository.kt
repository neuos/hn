package eu.neuhuber.hn.data.repo

import android.util.Log
import co.touchlab.kermit.Logger
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json

object HackerNewsRepository : NewsRepository {
    private val logger = Logger.withTag("HackerNewsRepository")
    private val client = HttpClient(Android) {
        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            url { protocol = URLProtocol.HTTPS }
            host = "hacker-news.firebaseio.com"
        }
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    this@HackerNewsRepository.logger.d(message)
                }
            }
            level = LogLevel.HEADERS
        }

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