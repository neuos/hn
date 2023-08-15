package eu.neuhuber.hn.ui.util

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import co.touchlab.kermit.Logger
import kotlin.math.min


/**
 * Change the font size of a Text to fit the available space.
 *
 * @param maxTextSize By default only shrinks the text. To also grow set this value.
 *
 * @see <a href="https://stackoverflow.com/a/76620563">Based on https://stackoverflow.com/a/76620563</a>
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    maxTextSize: TextUnit = style.fontSize,
) {
    var scaledTextStyle by remember { mutableStateOf(style) }
    val localDensity = LocalDensity.current
    Text(text = text,
        color = color,
        maxLines = maxLines,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = false,
        style = scaledTextStyle,
        onTextLayout = { result ->
            Logger.withTag("AutoSizeText")
                .d { "onTextLayout: '${result.layoutInput.text}' ${result.layoutInput.constraints}, maxTextSize: $maxTextSize" }
            val scaleByHeight = result.layoutInput.constraints.maxHeight / 2
            val scaleByWidth =
                result.layoutInput.constraints.maxWidth / result.layoutInput.text.length

            Logger.withTag("AutoSizeText")
                .d { "scaleByHeight: $scaleByHeight, scaleByWidth: $scaleByWidth" }
            val scaledSize = with(localDensity) {
                min(scaleByHeight, scaleByWidth).toSp()
            }
            scaledTextStyle = scaledTextStyle.copy(
                fontSize = scaledSize.coerceAtMost(maxTextSize)
            )
            Logger.withTag("AutoSizeText")
                .d { "'${result.layoutInput.text}' scaledSize: ${scaledTextStyle.fontSize}" }
        },
        modifier = modifier.drawWithContent { drawContent() })
}

private fun TextUnit.coerceAtMost(maximumValue: TextUnit) =
    if (this <= maximumValue) this else maximumValue

