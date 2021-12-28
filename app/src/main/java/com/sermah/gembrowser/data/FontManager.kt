package com.sermah.gembrowser.data

import android.content.res.AssetManager
import android.graphics.Typeface
import android.util.Log
import kotlin.io.path.Path
import kotlin.io.path.name

object FontManager {
    private const val fontsPath = "fonts/"
    private val typefaces: MutableMap<String, Typeface> = mutableMapOf()

    fun loadFonts(assets: AssetManager){
        assets.list(fontsPath)?.forEach{
            typefaces[it] = Typeface.createFromAsset(assets, fontsPath+it)
        }
    }

    fun get(name: String): Typeface?{
        return typefaces[name]
    }
}