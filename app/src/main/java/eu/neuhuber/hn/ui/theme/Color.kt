package eu.neuhuber.hn.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import co.touchlab.kermit.Logger
import eu.neuhuber.hn.R

enum class ResourceColor(val id: Int) {
    HNOrange(R.color.hn_orange),
    HNOrangeLight(R.color.hn_orange_light),
    HNGrey(R.color.hn_grey),
    ;

    @Composable
    fun load(): Color {
        Logger.withTag("ResourceColor").v("loading color resource for ${this.name}")
        return colorResource(id)
    }
}