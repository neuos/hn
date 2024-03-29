package eu.neuhuber.hn.ui.util

import android.net.Uri
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import eu.neuhuber.hn.ui.theme.HnPreviews
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun Favicon(
    uri: Uri,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: Painter? = null
) {
    val faviconUri = faviconUrl(uri)

    val placeholderIcon: @Composable (BoxScope.(Any) -> Unit)? =
        if (placeholder == null) null else ({
            Icon(painter = placeholder, contentDescription = contentDescription)
        })

    KamelImage(
        asyncPainterResource(data = faviconUri),
        contentDescription = contentDescription,
        onLoading = placeholderIcon,
        onFailure = placeholderIcon,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

private fun faviconUrl(uri: Uri) = Uri.Builder()
    .scheme(uri.scheme)
    .authority(uri.authority)
    .path("favicon.ico").build().toString()


@HnPreviews
@Composable
private fun FaviconPreview() {
    Favicon(
        uri = Uri.parse("https://news.ycombinator.com/"),
    )
}