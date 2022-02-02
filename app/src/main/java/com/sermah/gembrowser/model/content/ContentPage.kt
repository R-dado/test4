package com.sermah.gembrowser.model.content

import android.net.Uri
import java.lang.StringBuilder
import java.util.*

data class ContentPage(
    val uri: Uri,
    val favicon: String = "🌐",
    val header: String,
    var body: String,
    var lines: List<ContentLine> = listOf(),
) {
    var tooOld = false

    fun getTitle(): String {
        lines.forEach { line ->
            if (line.type == ContentLine.ContentType.H1) {
                val candidate = line.data
                if ( candidate.isNotBlank() ) return line.data.trim()
                    .replace("_", " ")
                    .split(" ").joinToString(separator = " ", transform = String::capitalize)
            }
        }
        val str = StringBuilder()
        str.append(uri.host ?: "")
        if (uri.pathSegments.size > 1) str.append("/…")
        if (uri.pathSegments.size > 0) str.append("/${uri.lastPathSegment}")
        return str.toString()
    }
}