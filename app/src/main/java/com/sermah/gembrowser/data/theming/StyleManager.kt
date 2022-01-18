package com.sermah.gembrowser.data.theming

import android.content.Context
import com.sermah.gembrowser.data.storage.SimpleDataStorage
import com.sermah.gembrowser.model.theming.AppColors
import com.sermah.gembrowser.model.theming.AppStyles
import com.sermah.gembrowser.model.theming.LineStyle

object StyleManager {
    val defaultLightStyles = AppStyles(AppColors())
    val defaultDarkStyles = AppStyles(AppColors().inverted())

    var loadedStyles = false
    var isDark = false
        set(v) { field = v; updateStyles() }
    var primaryStyles           : AppStyles = defaultLightStyles
    var darkStyles              : AppStyles? = defaultDarkStyles
    var currentStyles           : AppStyles = primaryStyles

    var styleText      : LineStyle = currentStyles.styleText
    var styleH1        : LineStyle = currentStyles.styleH1
    var styleH2        : LineStyle = currentStyles.styleH2
    var styleH3        : LineStyle = currentStyles.styleH3
    var styleLink      : LineStyle = currentStyles.styleLink
    var styleExtLink   : LineStyle = currentStyles.styleExtLink
    var styleQuote     : LineStyle = currentStyles.styleQuote
    var styleUl        : LineStyle = currentStyles.styleUl
    var styleEmpty     : LineStyle = currentStyles.styleEmpty
    var stylePre       : LineStyle = currentStyles.stylePre

    fun loadStyles(ctx: Context, primaryName: String? = null, darkName: String? = null) {
        // TODO: Do actual theme loading
        val dataReader = SimpleDataStorage(ctx)
        primaryStyles = AppStyles(AppColors(
            map = dataReader.readColors("themes/$primaryName/colors.dat")
        ))
        loadedStyles = true
    }

    fun updateStyles() {
        currentStyles = if (isDark && this.darkStyles != null) this.darkStyles!! else this.primaryStyles
        styleText      = currentStyles.styleText // Is there a way to avoid it? Otherwise these style* variables don't update and point to old currentStyles.style*
        styleH1        = currentStyles.styleH1
        styleH2        = currentStyles.styleH2
        styleH3        = currentStyles.styleH3
        styleLink      = currentStyles.styleLink
        styleExtLink   = currentStyles.styleExtLink
        styleQuote     = currentStyles.styleQuote
        styleUl        = currentStyles.styleUl
        styleEmpty     = currentStyles.styleEmpty
        stylePre       = currentStyles.stylePre
    }

    fun dpToPx(ctx: Context, dp: Float): Int {
        val scale: Float = ctx.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}