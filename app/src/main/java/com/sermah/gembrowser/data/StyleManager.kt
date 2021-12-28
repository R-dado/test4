package com.sermah.gembrowser.data

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import com.sermah.gembrowser.model.LineStyle

// Singleton
object StyleManager {

    var isDark = false

    val styleText = LineStyle ()
    val styleH1 = LineStyle (
        size    = 36f,
        //prefix  = "# ",
        padTop = 16f,
        padBottom = 16f,
        lineSpacing = 4f,
        style = LineStyle.TextStyle.BOLD,
        align = LineStyle.Alignment.CENTER,
    )
    val styleH2 = LineStyle (
        size    = 28f,
        //prefix  = "## ",
        padTop = 8f,
        padBottom = 8f,
        lineSpacing = 4f,
        style = LineStyle.TextStyle.BOLD,
    )
    val styleH3 = LineStyle (
        size    = 24f,
        //prefix  = "### ",
        style = LineStyle.TextStyle.BOLD,
    )
    val styleLink = LineStyle (
        padTop = 4f,
        padBottom = 4f,
        color   = LineStyle.TextColor(
            light   = Color.rgb(0x00, 0x77, 0xff),
            dark    = Color.rgb(0x22, 0x99, 0xff)),
        prefix  = "[↗] ",
    )
    val styleQuote = LineStyle (
        //prefix = "> ",
        padStart = 32f,
        style = LineStyle.TextStyle.ITALIC
    )
    val styleUl = LineStyle (
        prefix = "• ",
        padStart = 32f,
        padTop = 2f,
        padBottom = 2f,
    )
    val styleEmpty = LineStyle (
        padStart = 0f,
        padBottom = 0f,
        padEnd = 0f,
        padTop = 0f,
    )
    val stylePre = LineStyle (
        size = 14f,
        lineSpacing = 0f,
        padEnd = 4f,
        padTop = 4f,
        typeface = Typeface.MONOSPACE
    )

    fun dpToPx(ctx: Context, dp: Float): Int {
        val scale: Float = ctx.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}