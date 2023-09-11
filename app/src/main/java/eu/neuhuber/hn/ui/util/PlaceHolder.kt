package eu.neuhuber.hn.ui.util

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.fade
import com.google.accompanist.placeholder.material.placeholder

@Composable
fun CardPlaceholder(height: Dp) {
    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(height)
            .fillMaxHeight()
            .placeholder(
                color = placeholderColor(),
                visible = true, highlight = PlaceholderHighlight.fade(
                    highlightColor = fadeHighlightColor()
                )
            ),
    ) {
    }
}

@Composable
fun fadeHighlightColor(
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    alpha: Float = 0.3f,
): Color = backgroundColor.copy(alpha = alpha)

@Composable
fun placeholderColor(
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    contentAlpha: Float = 0.1f,
): Color = contentColor.copy(contentAlpha).compositeOver(backgroundColor)