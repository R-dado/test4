package com.sermah.gembrowser.model.theming

import android.graphics.Color
import android.util.Log
import androidx.core.math.MathUtils
import kotlin.math.roundToInt

data class AppColors (
    private var map:            Map<String, Int> = mapOf(),
    private var parentMap:      Map<String, Int> = mapOf(),
    var defaultText:            Int = Color.BLACK,
    var defaultBackground:      Int = Color.WHITE,
    ){

    // Map delegate for correct default values handling
    private val mapDg: Map<String, Int> = map.withDefault { name ->
        parentMap.withDefault { nameParent ->
            if (nameParent.contains("background", ignoreCase = true))
                defaultBackground else defaultText
        }[name] ?: Color.MAGENTA
    }

    // UI Colors
    val textPrimary                 :Int by mapDg
    val textSecondary               :Int by mapDg
    val textBottomTitle             :Int by mapDg
    val textBottomURI               :Int by mapDg
    val textSnackbar                :Int by mapDg
    val background                  :Int by mapDg
    val backgroundContainer         :Int by mapDg
    val backgroundBottom            :Int by mapDg
    val backgroundBottomURI         :Int by mapDg
    val backgroundSnackbar          :Int by mapDg
    val accent                      :Int by mapDg
    val iconNeutral                 :Int by mapDg

    // Content Colors
    val contentText                 :Int by mapDg
    val contentTextH1               :Int by mapDg
    val contentTextH2               :Int by mapDg
    val contentTextH3               :Int by mapDg
    val contentTextLink             :Int by mapDg
    val contentTextPre              :Int by mapDg
    val contentTextQuote            :Int by mapDg
    val contentTextList             :Int by mapDg
    val contentBackground           :Int by mapDg
    val contentBackgroundPre        :Int by mapDg
    val contentBackgroundQuote      :Int by mapDg

    fun makeChild() : AppColors {
        return AppColors(
            parentMap = map.toMap(),
            defaultBackground = defaultBackground,
            defaultText = defaultText,
        )
    }

    // Returns colors that are produced by inverting brightness of original colors.
    // If replaceParent provided and non-null, invertParent won't count.
    fun inverted(replaceParent: Map<String, Int>? = null, invertParent: Boolean = true): AppColors {
        return AppColors(
            map = map.mapValues { invertBrightness(it.value) },
            parentMap = replaceParent ?: if (invertParent)
                parentMap.mapValues { invertBrightness(it.value) } else parentMap.toMap(),
            defaultText = invertBrightness(defaultText),
            defaultBackground = invertBrightness(defaultBackground),
        )
    }

    companion object {
        // invert average value of rgb channels
        fun invertBrightness(color: Int): Int {
            var r = Color.red(color)
            var g = Color.green(color)
            var b = Color.blue(color)
            val luminance = getLuminance(color)
            return if (luminance > 0) {
                val k : Float = (255 - luminance).toFloat() / luminance

                r = MathUtils.clamp((r*k).roundToInt(), 0, 255)
                g = MathUtils.clamp((g*k).roundToInt(), 0, 255)
                b = MathUtils.clamp((b*k).roundToInt(), 0, 255)
                Color.argb(Color.alpha(color), r, g, b)
            } else{
                Color.argb(Color.alpha(color), 255, 255, 255)
            }
        }

        // Returns (3R + 4G + B) >> 3
        // https://stackoverflow.com/a/596241/10172462
        fun getLuminance(color: Int): Int {
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            return (3*r + 4*g + b) shr 3
        }

//        fun getLuminance(color: Int): Int {
//            val r = Color.red(color)
//            val g = Color.green(color)
//            val b = Color.blue(color)
//            return MathUtils.clamp(
//                (0.2126f*r + 0.7152f*g + 0.0722f*b).roundToInt(),
//                0, 255)
//        }
    }
}