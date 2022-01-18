package com.sermah.gembrowser.data.storage

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Color
import android.util.Log
import java.io.InputStreamReader

class SimpleDataStorage(private val ctx: Context) {
    fun readColors(fromPath: String) : Map<String, Int>{
        val reader = InputStreamReader(ctx.assets.open(fromPath, AssetManager.ACCESS_STREAMING))
        val map = mutableMapOf<String, Int>()
        reader.forEachLine {
            if (it.isNotBlank() and it.contains('=')) {
                val name = it.substringBefore('=').trim()
                val valueStr = it.substringAfter('=').trim()
                if (valueStr.isHex() && valueStr.length in setOf(3,4,6,8)) {
                    val color = when (valueStr.length) {
                        3 -> Color.rgb(
                            (valueStr.subSequence(0,1) as String).toInt(16) * 17,
                            (valueStr.subSequence(1,2) as String).toInt(16) * 17,
                            (valueStr.subSequence(2,3) as String).toInt(16) * 17,
                        )
                        4 -> Color.argb(
                            (valueStr.subSequence(0,1) as String).toInt(16) * 17,
                            (valueStr.subSequence(1,2) as String).toInt(16) * 17,
                            (valueStr.subSequence(2,3) as String).toInt(16) * 17,
                            (valueStr.subSequence(3,4) as String).toInt(16) * 17,
                        )
                        6 -> Color.rgb(
                            (valueStr.subSequence(0,2) as String).toInt(16),
                            (valueStr.subSequence(2,4) as String).toInt(16),
                            (valueStr.subSequence(4,6) as String).toInt(16),
                        )
                        8 -> Color.argb(
                            (valueStr.subSequence(0,2) as String).toInt(16),
                            (valueStr.subSequence(2,4) as String).toInt(16),
                            (valueStr.subSequence(4,6) as String).toInt(16),
                            (valueStr.subSequence(6,8) as String).toInt(16),
                        )
                        else -> -1
                    }
                    map[name] = color
                    Log.d("Colors parsing", "$name to ${"%x".format(color)} (original \"$valueStr\")")
                }
            }
        }
        return map.toMap()
    }

    fun CharSequence.isHex(): Boolean {
        this.forEach { ch -> if (ch.lowercaseChar() !in "0123456789abcdef") return false }
        return true
    }
}