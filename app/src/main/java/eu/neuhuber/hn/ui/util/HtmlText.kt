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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat
import co.touchlab.kermit.Logger

/**
 * A composable that renders a string of HTML text.
 * The HTML can contain only the HNN tags:
 * p, a, i, pre with code
 *
 * a tags are clickable and will open the link.
 */
@Composable
fun HtmlText(text: String, modifier: Modifier = Modifier) {
    val linkColor = MaterialTheme.colorScheme.primary
    val lineNumColor = LocalContentColor.current.copy(alpha = 0.5f)
    val uriHandler = LocalUriHandler.current

    val commentParts by remember(text, lineNumColor) {
        derivedStateOf { parseComment(text, lineNumColor) }
    }

    Column(modifier) {
        commentParts.forEach { part ->
            when (part) {
                is CommentPart.Paragraph -> {
                    Text(part.formattedContent(linkColor))
                }

                is CommentPart.CodeBlock -> {
                    Text(
                        part.formattedContent(linkColor),
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clip(MaterialTheme.shapes.small),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,


                        )
                }
            }
        }

    }
//    annotatedString.getStringAnnotations("CodeBlock", 0, annotatedString.length).forEach {
//        Logger.withTag("HtmlText").d { "Found code block: ${it.start to it.end}" }
//    }
//
//    annotatedString.getStringAnnotations("Co", 0, annotatedString.length).forEach {
//        Logger.withTag("HtmlText").d { "Found URL: ${it.item}" }
//    }
//
//    Text(modifier = modifier, text = annotatedString)
}



/**
 * parses the limited subset of html, that HN uses.
 * can only contain the following tags:
 *     <a href="https://example.com">Link</a>
 *     <i>Italic</i>
 *     <pre><code>   code block     </code></pre>
 *     <p>paragraph</p>
 */
fun parseComment(html: String, lineNumberColor: Color = Color.Gray): List<CommentPart> {
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
                ssb.appendCodeBlock(nextContent, lineNumberColor)
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
                    add(CommentPart.Paragraph(SpannedString(ssb)))
                    ssb = SpannableStringBuilder()
                }

                else -> Logger.withTag("HtmlText").e { "Unknown match: $match" }
            }
            i = match.range.last + 1
        }

        if (i < html.length) {
            ssb.append(html.substring(i).htmlDecoded())
        }
    }
}


sealed class CommentPart(private val content: SpannedString) {
    class Paragraph(content: SpannedString) : CommentPart(content)
    class CodeBlock(content: SpannedString) : CommentPart(content)

    fun formattedContent(linkColor: Color) = content.toAnnotatedString(linkColor)
}

private fun SpannableStringBuilder.appendCodeBlock(
    nextContent: String, lineNumberColor: Color
) {
    val lines = nextContent.split("\n")
    val lineDigits = lines.size.toString().length
    val commonLeadingWhitespace = lines.filter { it.isNotBlank() }
        .minOfOrNull { line -> line.takeWhile { it.isWhitespace() }.length } ?: 0

    lines.forEachIndexed { index, s ->
        val leadingWhitespace = s.drop(commonLeadingWhitespace).takeWhile { it.isWhitespace() }
        val trailingWhitespace = s.takeLastWhile { it.isWhitespace() }
        val lineNumber = (index+1).toString().padStart(lineDigits, ' ') + ' '
        append(lineNumber)
        setSpan(
            ForegroundColorSpan(lineNumberColor.toArgb()),
            length - lineNumber.length,
            length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        append(leadingWhitespace)
        appendLine(s.htmlDecoded())
        append(trailingWhitespace)
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
