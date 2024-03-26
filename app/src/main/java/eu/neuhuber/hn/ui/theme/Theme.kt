package eu.neuhuber.hn.ui.theme

import android.annotation.TargetApi
import android.content.res.Configuration
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.neuhuber.hn.ui.theme.ResourceColor.HNGrey
import eu.neuhuber.hn.ui.theme.ResourceColor.HNOrange
import eu.neuhuber.hn.ui.theme.ResourceColor.HNOrangeLight
import kotlin.math.ln

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

@TargetApi(Build.VERSION_CODES.S)
@Composable
fun dynamicColorScheme(dark: Boolean): ColorScheme = when {
    dark -> dynamicDarkColorScheme(LocalContext.current)
    else -> dynamicLightColorScheme(LocalContext.current)
}

@Composable
fun staticColorScheme(dark: Boolean): ColorScheme = when {
    dark -> hnDarkColorScheme()
    else -> hnLightColorScheme()
}

@Composable
fun hnColorScheme() = when {
    dynamic -> dynamicColorScheme(isSystemInDarkTheme())
    else -> staticColorScheme(isSystemInDarkTheme())
}

@Composable
fun HnTheme(content: @Composable () -> Unit) {
    val colorScheme = hnColorScheme()
    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current as ComponentActivity

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = Typography
    )

    LaunchedEffect(isDarkMode) {
        context.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                colorScheme.background.toArgb(),
                colorScheme.background.toArgb(),
            ), navigationBarStyle = SystemBarStyle.auto(
                colorScheme.navbar.toArgb(),
                colorScheme.navbar.toArgb(),
            )
        )
    }

}

val ColorScheme.navbar: Color
    get() {
        // taken from ColorScheme
        fun ColorScheme.surfaceColorAtElevation(
            elevation: Dp,
        ): Color {
            if (elevation == 0.dp) return surface
            val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
            return primary.copy(alpha = alpha).compositeOver(surface)
        }

        val defaultNavbarElevation = 3.dp
        return surfaceColorAtElevation(defaultNavbarElevation)
    }

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Light")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark")
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class HnPreviews
