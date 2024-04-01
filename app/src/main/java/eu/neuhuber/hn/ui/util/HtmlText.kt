package eu.neuhuber.hn.ui.util

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import co.touchlab.kermit.Logger
import eu.neuhuber.hn.ui.theme.HnPreviews

/**
 * A composable that renders a string of HTML text.
 * The HTML can contain only the HN tags:
 * p, a, i, pre with code
 *
 * a tags are clickable and will open the link.
 */
@Composable
fun HtmlText(text: String, modifier: Modifier = Modifier) {
    val linkColor = MaterialTheme.colorScheme.primary
    val lineNumColor = LocalContentColor.current.copy(alpha = 0.5f)
    val codeBlockBackgroundColor = MaterialTheme.colorScheme.surfaceVariant

    val commentParts by remember(text) {
        derivedStateOf { parseComment(text) }
    }

    Column(modifier) {
        commentParts.forEach { part ->
            val content = part.formattedContent(linkColor)
            when (part) {
                is CommentPart.Paragraph -> {
                    Text(content)
                }

                is CommentPart.CodeBlock -> {
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .height(IntrinsicSize.Min)
                            .background(codeBlockBackgroundColor)
                            .padding(4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            (1..part.lineCount).forEach {
                                Text(
                                    "$it", color = lineNumColor, textAlign = TextAlign.End,
                                )
                            }
                        }
                        Divider(modifier = Modifier
                            .fillMaxHeight(1f)
                            .padding(start = 4.dp, end = 4.dp)
                            .width(1.dp)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                content,
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .clip(MaterialTheme.shapes.small),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                }
            }
            Spacer(modifier =Modifier.height(4.dp))
        }
    }
}



/**
 * parses the limited subset of html, that HN uses.
 * can only contain the following tags:
 *     <a href="https://example.com">Link</a>
 *     <i>Italic</i>
 *     <pre><code>   code block     </code></pre>
 *     <p>paragraph</p>
 */
fun parseComment(html: String): List<CommentPart> {
    return buildList {
        var ssb = SpannableStringBuilder()

        val linkRegex = """<a href="(.+?)".*?>""".toRegex()
        val linkCloseRegex = """</a>""".toRegex()
        val italicRegex = """<i>""".toRegex()
        val italicCloseRegex = """</i>""".toRegex()
        val codeRegex = """<pre><code>""".toRegex()
        val codeCloseRegex = """</code></pre>""".toRegex()
        val paragraphRegex = """<p>""".toRegex()

        val regexes = listOf(
            linkRegex,
            linkCloseRegex,
            italicRegex,
            italicCloseRegex,
            codeRegex,
            codeCloseRegex,
            paragraphRegex
        )

        val matches = regexes.flatMap { regex ->
            regex.findAll(html).toList().map { match -> regex to match }
        }.sortedBy { (_, match) -> match.range.first }

        var i = 0
        for ((matcher, match) in matches) {

            val nextContent = html.substring(i, match.range.first)
            if (matcher != codeCloseRegex) {
                ssb.append(nextContent.htmlDecoded())
            } else {
                ssb.appendCodeBlock(nextContent)
            }

            when (matcher) {
                linkRegex -> {
                    val s = match.groupValues[1].htmlDecoded()
                    ssb.startSpan(URLSpan(s))
                }

                linkCloseRegex -> ssb.endSpan<URLSpan>()

                italicRegex -> ssb.startSpan(StyleSpan(Typeface.ITALIC))
                italicCloseRegex -> ssb.endSpan<StyleSpan>()

                codeRegex -> ssb.startSpan(TypefaceSpan("monospace"))
                codeCloseRegex -> {
                    ssb.endSpan<TypefaceSpan>()
                    add(CommentPart.CodeBlock(SpannedString(ssb)))
                    ssb = SpannableStringBuilder()
                }

                paragraphRegex -> {
                    if(ssb.isNotEmpty()){
                        add(CommentPart.Paragraph(SpannedString(ssb)))
                        ssb = SpannableStringBuilder()
                    }
                }

                else -> Logger.withTag("HtmlText").e { "Unknown match: $match" }
            }
            i = match.range.last + 1
        }

        if (i < html.length) {
            ssb.append(html.substring(i).htmlDecoded())
        }
        add(CommentPart.Paragraph(SpannedString(ssb)))
    }
}


sealed class CommentPart(private val content: SpannedString) {
    class Paragraph(content: SpannedString) : CommentPart(content)
    class CodeBlock(content: SpannedString) : CommentPart(content) {
        val lineCount = content.split("\n").dropLastWhile(String::isBlank).size
    }

    fun formattedContent(linkColor: Color): AnnotatedString = content.toAnnotatedString(linkColor)
}

private fun SpannableStringBuilder.appendCodeBlock(rawCodeBlock: String) {
    val lines = rawCodeBlock.split("\n").dropWhile(String::isBlank).dropLastWhile(String::isBlank)
    val commonLeadingWhitespace = lines.filter { it.isNotBlank() }
        .minOfOrNull { line -> line.takeWhile { it.isWhitespace() }.length } ?: 0

    lines.forEach { s ->
        // html decoded trims the text, so the leading whitespace has to be added manually
        val leadingWhitespace = s.drop(commonLeadingWhitespace).takeWhile { it.isWhitespace() }
        append(leadingWhitespace)
        appendLine(s.htmlDecoded())
    }
}

// apply the formatting from the spanned text to the annotated string
private fun Spanned.toAnnotatedString(linkColor: Color): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedString
    append(spanned.toString())
    getSpans(0, spanned.length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        when (span) {
            is StyleSpan -> if (span.style == Typeface.ITALIC) {
                this@buildAnnotatedString.addStyle(
                    SpanStyle(fontStyle = FontStyle.Italic), start, end
                )
            }

            is TypefaceSpan -> if (span.family == "monospace") {
                addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start, end)
                addStringAnnotation("CodeBlock", "", start, end)
            }

            is URLSpan -> {
                addLink(LinkAnnotation.Url(span.url), start, end)
                addStyle(
                    SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                    start,
                    end
                )
            }

            is ForegroundColorSpan -> addStyle(
                SpanStyle(color = Color(span.foregroundColor)), start, end
            )

            else -> {
                Logger.withTag("HtmlText").e { "Unknown span type: $span" }
            }
        }
    }
}


private fun SpannableStringBuilder.startSpan(marker: Any) {
    setSpan(marker, length, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
}

private inline fun <reified T> SpannableStringBuilder.endSpan() {
    val span = getSpans(0, length, T::class.java).lastOrNull()
    if (span == null) {
        Logger.withTag("HtmlText").e { "No span found for ${T::class.java.simpleName}" }
        return
    }
    val spanStart = getSpanStart(span)
    removeSpan(span)
    setSpan(span, spanStart, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}

private fun String.htmlDecoded() =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()

@HnPreviews
@Composable
private fun HtmlTextPreview() {

    val NOTIFY_SOCKET = "\$NOTIFY_SOCKET"
    val content =
        """That is all the protocol is. From <a href="https:&#x2F;&#x2F;www.freedesktop.org&#x2F;software&#x2F;systemd&#x2F;man&#x2F;latest&#x2F;sd_notify.html" rel="nofollow">https:&#x2F;&#x2F;www.freedesktop.org&#x2F;software&#x2F;systemd&#x2F;man&#x2F;latest&#x2F;sd_n...</a>:<p>&gt; These functions send a single datagram with the state string as payload to the socket referenced in the $NOTIFY_SOCKET environment variable.<p>The simplest implementation (pseudocode, no error handling, not guaranteed to compile), is something like:<p><pre><code>    const char *addrstr = getenv(&quot;NOTIFY_SOCKET&quot;);
    if (addrstr) {
        int fd = socket(AF_UNIX, SOCK_DGRAM, 0);
        struct sockaddr_un addr = { .sun_family = AF_UNIX };
        strncpy(addr.sun_path, sizeof(addr.sun_path), addrstr);
        connect(fd, (struct sockaddr*) &amp;addr);
        write(fd, &quot;READY=1&quot;);
        close(fd);
    }</code></pre>"""

    ElevatedCard(

    ) {
        HtmlText(text = content.trimIndent(), Modifier.padding(
            16.dp
        ),)
    }

}
