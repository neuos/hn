package eu.neuhuber.hn.ui.theme

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import eu.neuhuber.hn.R

enum class ResourceColor(val id: Int) {
    HNOrange(R.color.hn_orange),
    HNOrangeDark(R.color.hn_orange_dark),
    HNOrangeLight(R.color.hn_orange_light),
    HNGrey(R.color.hn_grey),
    ;

    @Composable
    fun load(): Color {
        Log.i("color", "loading color resource for ${this.name}")
        return colorResource(id)
    }
}