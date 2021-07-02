package eu.neuhuber.hn.ui.util

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder

@Composable
fun CardPlaceholder(height: Dp) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(height)
            .fillMaxHeight()
            .placeholder(visible = true, highlight = PlaceholderHighlight.fade()),
        elevation = 8.dp,
    ) {
    }
}