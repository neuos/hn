package eu.neuhuber.hn.ui.util

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import eu.neuhuber.hn.R

@Composable
fun Favicon(
    uri: Uri,
    contentDescription: String? = null,
    placeholder: Painter
) {
    val faviconUri = faviconUrl(uri)

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(faviconUri)
            .crossfade(true)
            .build(),
        placeholder = placeholder,
        error = placeholder,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
    )
}

private fun faviconUrl(uri: Uri) = Uri.Builder()
    .scheme(uri.scheme)
    .authority(uri.authority)
    .path("favicon.ico").build().toString()


@Preview
@Composable
fun FaviconPreview(){
    Favicon(
        uri = Uri.parse("https://uibk.ac.at"),
        placeholder = painterResource(id = R.drawable.ic_baseline_open_in_browser_24)
    )
}