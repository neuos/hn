package eu.neuhuber.hn.data.repo

import android.net.Uri
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Instant
import kotlin.random.Random

object FakeNewsRepository : NewsRepository {

    override suspend fun getItem(itemId: Id): Result<Item> {
        if (Random.nextFloat() < 0.3f) {
            return Result.failure(Exception("random error"))
        }
        return Result.success(randomItem(itemId))
    }

    override suspend fun getTopStories(): Result<List<Id>> {
        return Result.success((0..500).map(Int::toLong))
    }

    override suspend fun getNewStories(): Result<List<Id>> {
        return Result.success((500..1000).map(Int::toLong))
    }

    override suspend fun getBestStories(): Result<List<Id>> {
        return Result.success((1000..1500).map(Int::toLong))
    }
}

private suspend fun randomItem(itemId: Id): Item = withContext(Dispatchers.Default){
    val random = Random(itemId)
    delay(random.nextLong(0, 4000))
    return@withContext Item(
        id = randomId(random),
        title = randomText(random.nextInt(3, 8)),
        url = Uri.parse("https://${randomWord(random.nextInt(4, 9))}.com"),
        by = randomWord(random.nextInt(4, 10)),
        score = random.nextLong(-10, 100),
        descendants = random.nextLong(0, 1684),
        text = randomText(random.nextInt(10, 100)),
        kids = (0..random.nextInt(0, 10)).map { randomId(random) },
        time = Instant.now().minusSeconds(random.nextLong(0, 1000000))
    )
}

fun randomId(random: Random = Random): Id = random.nextLong(654312)

val letters = ('a'..'z')
fun randomText(wordCount: Int, random: Random = Random): String = buildString {
    repeat(wordCount) {
        append(randomWord(random.nextInt(3, 7), random))
        append(" ")
    }
}

fun randomWord(length: Int, random: Random = Random): String = buildString {
    repeat(length) { append(letters.random(random)) }
}
