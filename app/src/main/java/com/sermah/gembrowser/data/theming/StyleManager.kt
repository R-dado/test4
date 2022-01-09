package com.sermah.gembrowser.data.theming

import android.content.Context
import com.sermah.gembrowser.model.theming.AppColors
import com.sermah.gembrowser.model.theming.AppStyles
import com.sermah.gembrowser.model.theming.LineStyle

object StyleManager {
    val defaultLightStyles = AppStyles(AppColors())
    val defaultDarkStyles = AppStyles(AppColors().inverted())

    var isDark = false
        set(v) { field = v; updateStyles() }
    var primaryStyles           : AppStyles = defaultLightStyles
    var darkStyles              : AppStyles? = null

    lateinit var styleText      : LineStyle
    lateinit var styleH1        : LineStyle
    lateinit var styleH2        : LineStyle
    lateinit var styleH3        : LineStyle
    lateinit var styleLink      : LineStyle
    lateinit var styleExtLink   : LineStyle
    lateinit var styleQuote     : LineStyle
    lateinit var styleUl        : LineStyle
    lateinit var styleEmpty     : LineStyle
    lateinit var stylePre       : LineStyle

    init {
        loadStyles()
        updateStyles()
    }

    fun loadStyles(lightName: String? = null, darkName: String? = null) {
        // TODO: Do actual theme loading
        primaryStyles = defaultLightStyles
        darkStyles = defaultDarkStyles
    }

    fun updateStyles() {
        val currentStyles : AppStyles = if (isDark && this.darkStyles != null) this.darkStyles!! else this.primaryStyles
        styleText = currentStyles.styleText
        styleH1 = currentStyles.styleH1
        styleH2 = currentStyles.styleH2
        styleH3 = currentStyles.styleH3
        styleLink = currentStyles.styleLink
        styleExtLink = currentStyles.styleExtLink
        styleQuote = currentStyles.styleQuote
        styleUl = currentStyles.styleUl
        styleEmpty = currentStyles.styleEmpty
        stylePre = currentStyles.stylePre
    }

    fun dpToPx(ctx: Context, dp: Float): Int {
        val scale: Float = ctx.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}