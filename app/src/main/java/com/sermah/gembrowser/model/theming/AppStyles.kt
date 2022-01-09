package com.sermah.gembrowser.model.theming

import android.graphics.Typeface

data class AppStyles(
    var colors          : AppColors = AppColors(),
    var styleText       : LineStyle = LineStyle(),
    var styleH1         : LineStyle = LineStyle(),
    var styleH2         : LineStyle = LineStyle(),
    var styleH3         : LineStyle = LineStyle(),
    var styleLink       : LineStyle = LineStyle(),
    var styleExtLink    : LineStyle = LineStyle(),
    var styleQuote      : LineStyle = LineStyle(),
    var styleUl         : LineStyle = LineStyle(),
    var styleEmpty      : LineStyle = LineStyle(),
    var stylePre        : LineStyle = LineStyle(),
) {

    constructor(colors: AppColors): this(
        colors = colors,
        styleText = LineStyle (
            color = colors.ctPrimaryText
        ),
        styleH1 = LineStyle (
            size    = 36f,
            //prefix  = "# ",
            padTop = 16f,
            padBottom = 16f,
            lineSpacing = 4f,
            style = LineStyle.TextStyle.BOLD,
            align = LineStyle.Alignment.CENTER,
            color = colors.ctPrimaryText,
        ),
        styleH2 = LineStyle (
            size    = 28f,
            //prefix  = "## ",
            padTop = 8f,
            padBottom = 8f,
            lineSpacing = 4f,
            style = LineStyle.TextStyle.BOLD,
            color = colors.ctPrimaryText,
        ),
        styleH3 = LineStyle (
            size    = 24f,
            //prefix  = "### ",
            style = LineStyle.TextStyle.BOLD,
            color = colors.ctPrimaryText,
        ),
        styleLink = LineStyle (
            padTop = 4f,
            padBottom = 4f,
            color   = colors.ctLinkText,
            prefix  = "🔗 ",
        ),
        styleExtLink = LineStyle (
            padTop = 4f,
            padBottom = 4f,
            color   = colors.ctLinkText,
            prefix  = "📡 ",
        ),
        styleQuote = LineStyle (
            //prefix = "> ",
            padStart = 32f,
            style = LineStyle.TextStyle.ITALIC,
            color = colors.ctSecondaryText,
        ),
        styleUl = LineStyle (
            prefix = "• ",
            padStart = 32f,
            padTop = 2f,
            padBottom = 2f,
            color = colors.ctPrimaryText,
        ),
        styleEmpty = LineStyle (
            padStart = 0f,
            padBottom = 0f,
            padEnd = 0f,
            padTop = 0f,
            color = colors.ctPrimaryText,
        ),
        stylePre = LineStyle (
            size = 14f,
            lineSpacing = 0f,
            padBottom = 4f,
            padTop = 4f,
            typeface = Typeface.MONOSPACE,
            color = colors.ctPrimaryText,
            nowrap = true,
        ),
    )
}
