package com.sermah.gembrowser.dataclasses

import com.sermah.gembrowser.dataclasses.throwables.UnknownColorException
import kotlin.jvm.Throws

interface IPaletteData {

    val name: String
    val entries: Map<String, IPaletteColor>

    @Throws(UnknownColorException::class)
    fun get(key: String): IPaletteColor

    fun safeGet(key: String, fallback: IPaletteColor): IPaletteColor

}