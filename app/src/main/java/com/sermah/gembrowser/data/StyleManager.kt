package com.sermah.gembrowser.data

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import com.sermah.gembrowser.R
import com.sermah.gembrowser.model.style.AppStyle
import com.sermah.gembrowser.model.style.DayNightColor
import com.sermah.gembrowser.model.style.LineStyle

object StyleManager {

    var isDark = false

    var appStyle = AppStyle ()

    val colors = DayNightColor(
        light = R.attr.colorOnSurface,
        dark = R.attr.colorOnSurface
    )

    val styleText = LineStyle (
        color = colors
    )
    val styleH1 = LineStyle (
        size    = 36f,
        //prefix  = "# ",
        padTop = 16f,
        padBottom = 16f,
        lineSpacing = 4f,
        style = LineStyle.TextStyle.BOLD,
        align = LineStyle.Alignment.CENTER,
        color = colors,
    )
    val styleH2 = LineStyle (
        size    = 28f,
        //prefix  = "## ",
        padTop = 8f,
        padBottom = 8f,
        lineSpacing = 4f,
        style = LineStyle.TextStyle.BOLD,
        color = colors,
    )
    val styleH3 = LineStyle (
        size    = 24f,
        //prefix  = "### ",
        style = LineStyle.TextStyle.BOLD,
        color = colors,
    )
    val styleLink = LineStyle (
        padTop = 4f,
        padBottom = 4f,
        color   = DayNightColor(
            light   = R.color.content_link,
            dark    = R.color.content_link,
        ),
        prefix  = "🔗 ",
    )
    val styleExtLink = LineStyle (
        padTop = 4f,
        padBottom = 4f,
        color   = DayNightColor(
            light   = R.color.content_link,
            dark    = R.color.content_link,
        ),
        prefix  = "📡 ",
    )
    val styleQuote = LineStyle (
        //prefix = "> ",
        padStart = 32f,
        style = LineStyle.TextStyle.ITALIC,
        color = DayNightColor(
            light = R.attr.colorOnSurface,
            dark = R.attr.colorOnSurface,
        ),
    )
    val styleUl = LineStyle (
        prefix = "• ",
        padStart = 32f,
        padTop = 2f,
        padBottom = 2f,
        color = colors,
    )
    val styleEmpty = LineStyle (
        padStart = 0f,
        padBottom = 0f,
        padEnd = 0f,
        padTop = 0f,
        color = colors,
    )
    val stylePre = LineStyle (
        size = 14f,
        lineSpacing = 0f,
        padBottom = 4f,
        padTop = 4f,
        typeface = Typeface.MONOSPACE,
        color = colors,
        nowrap = true
    )

    fun dpToPx(ctx: Context, dp: Float): Int {
        val scale: Float = ctx.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}