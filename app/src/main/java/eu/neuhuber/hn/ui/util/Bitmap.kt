package eu.neuhuber.hn.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import co.touchlab.kermit.Logger

fun createBitmap(context: Context, resourceId: Int): Bitmap {
    Logger.withTag("createBitmap").d { "creating bitmap from resource $resourceId" }
    val drawable: Drawable = AppCompatResources.getDrawable(context, resourceId)
        ?: throw Exception("could not find resource with id $resourceId")
    val canvas = Canvas()
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    drawable.draw(canvas)
    Logger.withTag("createBitmap").d { "bitmap created" }

    return bitmap
}
