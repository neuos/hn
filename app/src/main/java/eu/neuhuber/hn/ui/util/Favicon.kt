package eu.neuhuber.hn.ui.util

import android.net.Uri
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState

@Composable
fun Favicon(
    uri: Uri,
    contentDescription: String? = null,
    placeHolder: @Composable () -> Unit
) {
    val faviconUri = faviconUrl(uri)

    val painter = rememberCoilPainter(faviconUri)
    Log.i("Favicon", "loading for uri $uri")

    Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        )
        Crossfade(targetState = painter.loadState) {
            if (it !is ImageLoadState.Success) placeHolder()
        }
    }
}

private fun faviconUrl(uri: Uri) = Uri.Builder()
    .scheme(uri.scheme)
    .authority(uri.authority)
    .path("favicon.ico").build().toString()