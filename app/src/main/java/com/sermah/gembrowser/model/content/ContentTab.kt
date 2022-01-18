package com.sermah.gembrowser.model.content

import android.net.Uri
import android.util.Log
import com.sermah.gembrowser.data.content.ContentManager
import java.lang.StringBuilder

class ContentTab(
    var currentPage: ContentPage = ContentPage(uri = Uri.parse("/"), header = "", body = ""),

    var history: MutableList<ContentPage> = mutableListOf(), // 0..n, old -> new
    var currentIndex: Int = history.size - 1
) {
    val backSize: Int       get() = currentIndex
    val forwardSize: Int    get() = history.size - 1 - currentIndex

    // Go n pages back in history or -n pages forward. Returns null if out of borders,
    fun historyTravel(n: Int): ContentPage? {
        if (n > backSize || -n > forwardSize) return null

        currentIndex -= n
        currentPage = history[currentIndex]
        if (currentPage.tooOld) {
            ContentManager.requestUri(currentPage.uri, true)
        }
        //logHistory()

        return currentPage
    }

    fun openPage(p: ContentPage) {
        history = history.subList(0, currentIndex + 1)
        history.add(p)
        historyTravel(-1)

        //logHistory()
    }

    fun cleanOldBodies(olderThan: Int) {
        val ot = if (olderThan >= 0) olderThan else 0

        var i = 0; while (i < backSize - ot) {
            history[i].body = ""
            history[i].lines = listOf()
            history[i].tooOld = true
            i++
        }
    }

    fun logHistory() {
        val s = StringBuilder()
        history.forEachIndexed { i, p ->
            if (i == currentIndex) s.append("HERE -> ")
            s.appendLine("${i}: ${p.uri}")
        }
        Log.d("History", s.toString())
    }
}