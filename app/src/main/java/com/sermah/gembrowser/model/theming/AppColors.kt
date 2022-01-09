package com.sermah.gembrowser.model.theming

import android.graphics.Color
import android.util.Log
import androidx.core.math.MathUtils
import kotlin.math.roundToInt

// When adding new colors, don't forget to add them in `inverted()` function!!!
data class AppColors (
    // UI Colors
    var uiPrimaryText:          Int = Color.BLACK,
    var uiSecondaryText:        Int = Color.DKGRAY,
    var uiBackground:           Int = Color.WHITE,
    var uiContainerBackground:  Int = Color.LTGRAY,

    // Content Colors
    var ctPrimaryText:          Int = Color.BLACK,
    var ctSecondaryText:        Int = Color.DKGRAY,
    var ctLinkText:             Int = Color.rgb(0x00, 0x77, 0xFF),
    var ctBackground:           Int = Color.WHITE,
    var ctPreBackground:        Int = Color.LTGRAY,
) {

    // invert average value of rgb channels
    private fun invertBrightness(color: Int): Int {
        var r = Color.red(color)
        var g = Color.green(color)
        var b = Color.blue(color)
        val toInvert = (r + g + b) / 3
        return if (toInvert > 0) {
            val k : Float = (255 - toInvert).toFloat() / toInvert

            if (k == 0f)
                Log.d("Colors", "k == 0 at color ($r, $g, $b)!")

            r = MathUtils.clamp((r*k).roundToInt(), 0, 255)
            g = MathUtils.clamp((g*k).roundToInt(), 0, 255)
            b = MathUtils.clamp((b*k).roundToInt(), 0, 255)
            Color.argb(Color.alpha(color), r, g, b)
        } else{
            Color.argb(Color.alpha(color), 255, 255, 255)
        }
    }

    fun inverted(): AppColors {
        return AppColors(
            uiPrimaryText = invertBrightness(this.uiPrimaryText),
            uiSecondaryText = invertBrightness(this.uiSecondaryText),
            uiBackground = invertBrightness(this.uiBackground),
            uiContainerBackground = invertBrightness(this.uiContainerBackground),

            ctPrimaryText = invertBrightness(this.ctPrimaryText),
            ctSecondaryText = invertBrightness(this.ctSecondaryText),
            ctLinkText = invertBrightness(this.ctLinkText),
            ctBackground = invertBrightness(this.ctBackground),
            ctPreBackground = invertBrightness(this.ctPreBackground),
        )
    }
}