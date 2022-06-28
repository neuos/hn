package eu.neuhuber.hn.ui.theme

import android.annotation.TargetApi
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
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


@TargetApi(Build.VERSION_CODES.S)
@Composable
fun dynamicColorScheme(dark: Boolean): ColorScheme {
    return if (dark) dynamicDarkColorScheme(LocalContext.current)
    else dynamicLightColorScheme(LocalContext.current)
}

@Composable
fun hnLightColorScheme() = lightColorScheme(
    primary = HNOrange.load(),
    surface = HNGrey.load()
)

@Composable
fun HnTheme(isDark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (dynamic) {
        dynamicColorScheme(isDark)
    } else {
        if (isDark) hnDarkColorScheme() else hnLightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = Typography
    )

    val systemUiController = rememberSystemUiController()
    val systemBarColor = colorScheme.background

    Log.d("HnTheme", "")

    SideEffect {
        // dark icons if we're in light theme
        systemUiController.setStatusBarColor(
            color = colorScheme.background,
        )
        systemUiController.setNavigationBarColor(
            color = colorScheme.navbar,
        )

        // setStatusBarsColor() and setNavigationBarColor() also exist
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

