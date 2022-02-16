package com.sermah.gembrowser.model.theming

import android.content.res.AssetManager
import android.graphics.Typeface

object FontManager {
    private const val fontsPath = "fonts/"
    private val typefaces: MutableMap<String, Typeface> = mutableMapOf()

    fun loadFonts(assets: AssetManager){
        assets.list(fontsPath)?.forEach{
            typefaces[it] = Typeface.createFromAsset(assets, fontsPath +it)
        }
    }

    fun get(name: String): Typeface?{
        return typefaces[name]
    }
}