package com.sermah.gembrowser.data.content

import android.net.Uri
import android.util.Log
import com.sermah.gembrowser.model.content.ContentManager
import java.lang.StringBuilder

class Tab(
    var currentPage: IPage = placeholderPage(),

    var history: MutableList<IPage> = mutableListOf(), // 0..n, old -> new
    var currentIndex: Int = history.size - 1
) {
    val backSize: Int       get() = currentIndex
    val forwardSize: Int    get() = history.size - 1 - currentIndex

    // Go n pages back in history or -n pages forward. Returns null if out of borders,
    fun historyTravel(n: Int): IPage? {
        if (n > backSize || -n > forwardSize) return null

        currentIndex -= n
        currentPage = history[currentIndex]
        if (currentPage.old) {
            ContentManager.requestUri(currentPage.data.uri, true)
        }
        //logHistory()

        return currentPage
    }

    fun openPage(p: IPage) {
        history = history.subList(0, currentIndex + 1)
        history.add(p)
        historyTravel(-1)

        //logHistory()
    }

    fun makePagesOld(olderThan: Int) {
        val ot = if (olderThan >= 0) olderThan else 0

        for (i in 0 until backSize - ot) {
            history[i].old = true // onBecomeOld is called
        }
    }

    fun logHistory() {
        val s = StringBuilder()
        history.forEachIndexed { i, p ->
            if (i == currentIndex) s.append("HERE -> ")
            s.appendLine("${i}: ${p.data.uri}")
        }
        Log.d("History", s.toString())
    }

    companion object {
        fun placeholderPage() : GeminiPage = GeminiPage(
            GeminiResponse(
                header = "20 text/gemini",
                body = ""
            ),
            PageData(
                title = "New Tab",
                icon = "🛰️",
                uri = Uri.EMPTY,
                isBookmark = false,
            )
        )
    }
}