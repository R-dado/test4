package com.sermah.gembrowser.model.parsing

import android.net.Uri
import com.sermah.gembrowser.util.UriTools.addRelative
import com.sermah.gembrowser.data.content.ContentLine
import com.sermah.gembrowser.data.content.ContentLine.ContentType.*

object GmiParser {
    fun parse(gmi: String, absUri: Uri): List<ContentLine> {
        val gmiLen = gmi.length
        val result = mutableListOf<ContentLine>()

        var pre = false // preformat flag
        var preLines = ""

        for (line in gmi.lines()) {
            //Log.d("Parser", line)
            if (pre) {
                if (line.startsWith("```")){
                    result.add(ContentLine(preLines, PRE))
                    pre = false
                } else preLines += (if (preLines != "") "\n" else "") + line
            } else {
                var type = TEXT
                var addLine = true
                var off = 0
                var text = ""
                var extra = ""
                when (true) {
                    line.startsWith("```") -> {
                        pre = true
                        preLines = ""
                        addLine = false
                        extra = line.substring(3).trim()
                    }
                    line.startsWith("###") -> {
                        type = H3
                        off = 3
                    }
                    line.startsWith("##") -> {
                        type = H2
                        off = 2
                    }
                    line.startsWith("#") -> {
                        type = H1
                        off = 1
                    }
                    line.startsWith("*") -> {
                        type = UL
                        off = 1
                    }
                    line.startsWith(">") -> {
                        type = QUOTE
                        off = 1
                    }
                    line.startsWith("=>") -> {
                        type = LINK
                        off = 2
                        val fmtd = line.substring(off).replace("\\s+".toRegex(), " ").trim()
                        val link = fmtd.substringBefore(' ').trim()
                        text = fmtd.substringAfter(' ').trim()
                        var uri = Uri.parse(link)
                        if (uri.isRelative) {
                            uri = absUri.addRelative(uri)
                        }
                        extra = uri.normalizeScheme().toString()
                    }
                    line.isBlank() -> {
                        type = EMPTY
                    }
                    else -> {}
                }
                if (text.isEmpty()) text = line.substring(off)
                if (addLine) result.add(ContentLine(text, type, extra))
            }
        }
        return result
    }
}