package com.sermah.gembrowser.dataclasses

import com.sermah.gembrowser.dataclasses.throwables.UnknownColorException

class PaletteData(
    override val name: String,
    private var _entries: Map<String, IPaletteColor>
    ) : IPaletteData {

    override val entries : Map<String, IPaletteColor> by ::_entries

    override fun get(key: String): IPaletteColor {
        return _entries.getOrElse(key) {
            throw UnknownColorException(key)
        }
    }

    override fun safeGet(key: String, fallback: IPaletteColor): IPaletteColor {
        return _entries.getOrDefault(key, fallback)
    }

}