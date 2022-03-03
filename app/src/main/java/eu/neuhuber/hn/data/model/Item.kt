package eu.neuhuber.hn.data.model

import android.net.Uri
import eu.neuhuber.hn.data.EpochSecondInstantSerializer
import eu.neuhuber.hn.data.UriSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

enum class ItemType {
    job, story, comment, poll, pollopt
}

typealias Id = Long

@Serializable
data class Item(
    val id: Id,
    val deleted: Boolean? = null,
    val type: ItemType? = null,
    val by: String? = null,
    val text: String? = null,
    val dead: Boolean? = null,
    val parent: Id? = null,
    val poll: Id? = null,
    val kids: List<Id>? = null,
    @Serializable(with = UriSerializer::class)
    val url: Uri? = null,
    val score: Long? = null,
    val title: String? = null,
    val parts: List<Id>? = null,
    val descendants: Long? = null,
    @Serializable(with = EpochSecondInstantSerializer::class)
    val time: Instant? = null
)

