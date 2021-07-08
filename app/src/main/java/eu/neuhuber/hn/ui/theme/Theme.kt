package eu.neuhuber.hn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import eu.neuhuber.hn.ui.theme.ResourceColor.*

@Composable
fun darkColorPalette() = darkColors(
    primary = HNOrangeLight.load(),
    primaryVariant = HNOrangeDark.load(),
)

@Composable
fun lightColorPalette() = lightColors(
    primary =   HNOrange.load(),
    primaryVariant = HNOrangeDark.load(),
    surface = HNGrey.load()

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun HnTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        darkColorPalette()
    } else {
        lightColorPalette()
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
