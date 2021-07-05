package eu.neuhuber.hn.data.repo

import android.net.Uri
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

class FakeNewsRepository : NewsRepository {


    override suspend fun getItem(itemId: Id): Result<Item> {
        return Result.success(randomItem())
    }
}


val fakeItems: List<Item> by lazy {
    List(16) {
        randomItem()
    }
}

private fun randomItem() = Item(
    id = Random.nextLong(654312),
    title = randomTitle(Random.nextInt(3, 8)),
    url = Uri.parse("https://${randomWord(Random.nextInt(4, 9))}.com"),
    by = randomWord(Random.nextInt(4, 10)),
    score = Random.nextLong(-10, 100),
    descendants = Random.nextLong(0, 1684)
)

val letters = ('a'..'z')
fun randomTitle(wordCount: Int): String = buildString {
    repeat(wordCount){ append(randomWord(Random.nextInt(3,7)))
    append(" ")}
}
fun randomWord(length: Int): String = buildString {
    repeat(length) { append(letters.random()) }
}
