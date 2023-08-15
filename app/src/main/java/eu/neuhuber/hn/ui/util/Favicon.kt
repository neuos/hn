package eu.neuhuber.hn.ui.util

import android.net.Uri
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import eu.neuhuber.hn.ui.theme.HnPreview
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun Favicon(
    uri: Uri, contentDescription: String? = null, placeholder: Painter? = null
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
    )
}

private fun faviconUrl(uri: Uri) = Uri.Builder()
    .scheme(uri.scheme)
    .authority(uri.authority)
    .path("favicon.ico").build().toString()


@HnPreview
@Composable
fun FaviconPreview() {
    Favicon(
        uri = Uri.parse("https://uibk.ac.at")
    )
}