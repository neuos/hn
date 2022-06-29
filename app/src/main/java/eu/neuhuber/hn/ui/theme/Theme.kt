package eu.neuhuber.hn.ui.theme

import android.annotation.TargetApi
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import eu.neuhuber.hn.ui.theme.ResourceColor.*
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

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = Typography
    )

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = colorScheme.background,
        )
        systemUiController.setNavigationBarColor(
            color = colorScheme.navbar,
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


@Composable
fun ColoredTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = hnColorScheme(),
    typography = Typography,
    content = content
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Light")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark")
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
annotation class HnPreview
