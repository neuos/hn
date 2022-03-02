package eu.neuhuber.hn.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import eu.neuhuber.hn.ui.theme.ResourceColor.*

val dynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun hnDarkColorScheme() = darkColorScheme(
    primary = HNOrangeLight.load(),
)

@Composable
fun hnLightColorScheme() = lightColorScheme(
    primary = HNOrange.load(),
    surface = HNGrey.load()
)

@Composable
fun HnTheme(isDark: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colorScheme = if (dynamic) {
        val context = LocalContext.current
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDark) hnDarkColorScheme() else hnLightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = Typography
    )
}
