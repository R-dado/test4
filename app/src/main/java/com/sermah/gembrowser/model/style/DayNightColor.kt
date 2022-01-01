package com.sermah.gembrowser.model.style

import android.graphics.Color

data class DayNightColor(
    val light: Int = Color.WHITE,
    val dark: Int = Color.BLACK
) {
    companion object {
        val TextDefault = DayNightColor(
            light = Color.BLACK,
            dark = Color.WHITE
        )

        fun swap(color: DayNightColor) : DayNightColor {
            return DayNightColor(light = color.dark, dark = color.light)
        }
    }
}
