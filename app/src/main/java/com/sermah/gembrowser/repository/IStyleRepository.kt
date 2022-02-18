package com.sermah.gembrowser.repository

import com.sermah.gembrowser.dataclasses.IStyleData

interface IStyleRepository {

    fun getCurrentAppStyle() : IStyleData
    fun getCurrentPageStyle() : IStyleData

    fun getAppStylesData() : IStyleData
    fun getPageStylesData() : IStyleData

    fun notifyDarkMode(isDark: Boolean)
    fun loadStyles(loadDark: Boolean)

}