package com.sermah.gembrowser.data.content

import com.sermah.gembrowser.model.parsing.GmiParser
import java.lang.StringBuilder

data class GeminiPage(
    var gemResponse: GeminiResponse,
    override var data: PageData,
): IPage {
    override var response: IResponse = gemResponse
    override var content = GmiParser.parse(gemResponse.body, data.uri)
    override var source = gemResponse.body
    override var old = false
        set(f) { field = if (f) { onBecomeOld(); f } else f } // just call onBecomeOld if setting true

    init {
        updateData()
    }

    override fun updateData() {
        data.title = findTitle()
        data.mimeType = gemResponse.meta
    }

    private fun findTitle(): String {
        content.forEach { line ->
            if (line.type == ContentLine.ContentType.H1) {
                val candidate = line.data
                if ( candidate.isNotBlank() ) return line.data.trim()
                    .replace("_", " ")
                    .split(" ").joinToString(separator = " ", transform = String::capitalize)
            }
        }
        val str = StringBuilder()
        str.append(data.uri.host ?: "")
        if (data.uri.pathSegments.size > 1) str.append("/…")
        if (data.uri.pathSegments.size > 0) str.append("/${data.uri.lastPathSegment}")
        return str.toString()
    }

    private fun onBecomeOld() {
        content = listOf()
        gemResponse = GeminiResponse("","")
    }
}