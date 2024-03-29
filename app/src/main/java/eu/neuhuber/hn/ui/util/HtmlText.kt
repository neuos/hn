package eu.neuhuber.hn.ui.util

import android.graphics.Typeface
import android.net.Uri
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import co.touchlab.kermit.Logger
import org.xml.sax.XMLReader


fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedString
    append(spanned.toString())
    getSpans(0, spanned.length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        when (span) {
            is StyleSpan -> if (span.style == Typeface.ITALIC) {
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
            }

            is TypefaceSpan -> if (span.family == "monospace") {
                addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start, end)
            }

            is URLSpan -> addStringAnnotation(
                "URL", span.url, start, end
            ).also {
                addStyle(
                    SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                    start,
                    end
                )
            }


            else -> {
                Logger.withTag("HtmlText").e { "Unknown span type: $span" }
            }
        }
    }
}

class PreSpan

@Composable
fun HtmlText(text: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val linkColor = MaterialTheme.colorScheme.primary
    val fontSize = MaterialTheme.typography.bodyMedium.fontSize

    val spanned by remember(text) {
        derivedStateOf {
            HtmlCompat.fromHtml(
                text,
                HtmlCompat.FROM_HTML_MODE_COMPACT,
                null
            ) { opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader ->
                if (tag == "pre") {
                    val pos = output.length
                    if (opening) {
                        output.setSpan(PreSpan(), pos, pos, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    } else {
                        output.getSpans<PreSpan>().lastOrNull()?.let {
                            val start = output.getSpanStart(it)
                            output.setSpan(
                                TypefaceSpan("monospace"),
                                start,
                                pos,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
            }
        }
    }

    val annotatedString by remember(spanned) {
        derivedStateOf {
            spanned.toAnnotatedString()
        }
    }
    ClickableText(modifier = modifier, text = annotatedString, onClick = { offset ->
        annotatedString.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
            val uri = Uri.parse(it.item)
            Logger.withTag("HtmlText").d { "Opening URL: $uri" }
        }
    })/*
       AndroidView(factory = {
            MaterialTextView(context).apply {
                setText(spanned)
                textSize = fontSize.value
                setLinkTextColor(linkColor.toArgb())
                movementMethod = LinkMovementMethod.getInstance()
            }
        })
        */
}
