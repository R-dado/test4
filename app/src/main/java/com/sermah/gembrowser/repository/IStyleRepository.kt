package com.sermah.gembrowser.repository

import com.sermah.gembrowser.dataclasses.IPaletteData

interface IStyleRepository {

    fun getCurrentAppPalette() : IPaletteData
    fun getCurrentPagePalette() : IPaletteData

    fun getAppPaletteData() : IPaletteData
    fun getPagePaletteData() : IPaletteData

    fun notifyDarkMode(isDark: Boolean)
    fun loadStyles(loadDark: Boolean)

}