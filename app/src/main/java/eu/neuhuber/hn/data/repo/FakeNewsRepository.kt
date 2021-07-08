package eu.neuhuber.hn.data.repo

import android.net.Uri
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import kotlin.random.Random

class FakeNewsRepository : NewsRepository {

    override suspend fun getItem(itemId: Id): Result<Item> {
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

private fun randomItem(itemId: Id): Item {
    val random = Random(itemId)
    return Item(
        id = random.nextLong(654312),
        title = randomTitle(random.nextInt(3, 8)),
        url = Uri.parse("https://${randomWord(random.nextInt(4, 9))}.com"),
        by = randomWord(random.nextInt(4, 10)),
        score = random.nextLong(-10, 100),
        descendants = random.nextLong(0, 1684)
    )
}

val letters = ('a'..'z')
fun randomTitle(wordCount: Int): String = buildString {
    repeat(wordCount) {
        append(randomWord(Random.nextInt(3, 7)))
        append(" ")
    }
}

fun randomWord(length: Int, random: Random = Random): String = buildString {
    repeat(length) { append(letters.random(random)) }
}
