package eu.neuhuber.hn.ui.util

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun Favicon(
    uri: Uri,
    contentDescription: String? = null,
    placeHolder: Painter
) {
    val faviconUri = faviconUrl(uri)

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(faviconUri)
            .crossfade(true)
            .build(),
        placeholder = placeHolder,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop
    )
}

private fun faviconUrl(uri: Uri) = Uri.Builder()
    .scheme(uri.scheme)
    .authority(uri.authority)
    .path("favicon.ico").build().toString()