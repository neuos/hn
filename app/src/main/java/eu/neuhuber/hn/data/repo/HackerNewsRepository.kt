package eu.neuhuber.hn.data.repo

import co.touchlab.kermit.Logger
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
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
import io.ktor.client.plugins.logging.Logger as KtorLogger

object HackerNewsRepository : NewsRepository {
    private val logger = Logger.withTag("HackerNewsRepository")
    private val client = HttpClient() {
        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            url { protocol = URLProtocol.HTTPS }
            host = "hacker-news.firebaseio.com"
        }
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = object : KtorLogger {
                override fun log(message: String) {
                    this@HackerNewsRepository.logger.d(message)
                }
            }
            level = LogLevel.HEADERS
        }
        install(UserAgent) {
            agent = "ktor - eu.neuhuber.hn"
        }
        install(HttpTimeout){
            requestTimeoutMillis = 4000
        }
    }

    private suspend inline fun <reified T> tryGet(path: String): Result<T> = try {
        val httpResponse = client.get(urlString = path)
        when(httpResponse.status.value) {
            200 -> Result.success(httpResponse.body())
            else -> {
                logger.e { "get request to $path failed with status ${httpResponse.status}" }
                Result.failure(Exception("get request to $path failed with status ${httpResponse.status}"))
            }
        }
    } catch (e: Throwable) {
        logger.e(e) { "get request to $path failed" }
        Result.failure(e)
    }

    override suspend fun getTopStories(): Result<List<Id>> = tryGet("v0/topstories.json")

    override suspend fun getItem(itemId: Id): Result<Item> = tryGet("v0/item/$itemId.json")

    override suspend fun getNewStories(): Result<List<Id>> = tryGet("/v0/newstories.json")

    override suspend fun getBestStories(): Result<List<Id>> = tryGet("/v0/beststories.json")
}