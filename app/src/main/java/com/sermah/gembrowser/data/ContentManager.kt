package com.sermah.gembrowser.data

import android.net.Uri
import com.sermah.gembrowser.data.navigation.ContentTab
import com.sermah.gembrowser.data.network.GeminiClient
import com.sermah.gembrowser.data.parsing.GmiParser
import com.sermah.gembrowser.model.ContentLine
import com.sermah.gembrowser.model.ContentPage

object ContentManager {

    const val CONTENT_HISTORY_SIZE = 4

    var currentTab: ContentTab = ContentTab()
    var tabs: MutableList<ContentTab> = mutableListOf(currentTab)

    var contentUpdateHandler: ContentUpdateHandler = ContentUpdateHandler()
    var onNonGeminiScheme: (Uri) -> Unit = {}

    fun requestUri(uri: Uri, saveCurrentInHistory: Boolean = true) {
        if (uri.scheme != "gemini") onNonGeminiScheme(uri)
        else GeminiClient.connectAndRetrieve(
            uri = uri,
            onSuccess = fun(header, body) {
                val meta = header.substring(3)
                var lines: List<ContentLine>
                if (meta.startsWith("text/gemini")) {
                    lines = GmiParser.parse(body, uri)
                } else if (meta.startsWith("text")) {
                    lines = listOf(ContentLine(body, ContentLine.ContentType.TEXT))
                } else lines = listOf()
                contentUpdateHandler.onSuccess(meta, lines)
                addToHistory(ContentPage(uri = uri, lines = lines, header = header, body = body))
            },
            onNotSuccess = fun(header) {
                if (header.length < 2) return
                val meta = if (header.length > 3) header.substring(3) else ""
                val code = header.substring(0,2)
                when (code[0]) {
                    '1' -> contentUpdateHandler.onInput         (code, meta, uri)
                    '3' -> contentUpdateHandler.onRedirect      (code, meta, uri)
                    '4' -> contentUpdateHandler.onTemporary     (code, meta, uri)
                    '5' -> contentUpdateHandler.onPermanent     (code, meta, uri)
                    '6' -> contentUpdateHandler.onCertificate   (code, meta, uri)
                }
            },
        )
    }

    fun loadPreviousPage(): Boolean {
        val page = currentTab.historyTravel(1) ?: return false
        val meta = if (page.header.length > 2) page.header.substring(3) else ""
        var lines = page.lines
        if (lines.isEmpty()){
            lines = when {
                meta.startsWith("text/gemini") -> {
                    GmiParser.parse(page.body, page.uri)
                }
                meta.startsWith("text") -> {
                    listOf(ContentLine(page.body, ContentLine.ContentType.TEXT))
                }
                else -> listOf()
            }
        }
        currentTab.currentPage = page
        contentUpdateHandler.onSuccess(meta, lines)

        return true
    }

    fun addToHistory(page: ContentPage) {
        currentTab.writeInHistory(page)
        currentTab.cleanOldBodies(CONTENT_HISTORY_SIZE)
    }

    class ContentUpdateHandler (
        val onSuccess    : (mime: String, content: List<ContentLine>) -> Unit = {_, _ -> }, // 2x status codes
        val onInput      : (code: String, meta: String, uri: Uri) -> Unit = {_, _, _ -> }, // 1x status codes
        val onRedirect   : (code: String, meta: String, uri: Uri) -> Unit = {_, _, _ -> }, // 3x status codes
        val onTemporary  : (code: String, meta: String, uri: Uri) -> Unit = {_, _, _ -> }, // 4x status codes
        val onPermanent  : (code: String, meta: String, uri: Uri) -> Unit = {_, _, _ -> }, // 5x status codes
        val onCertificate: (code: String, meta: String, uri: Uri) -> Unit = {_, _, _ -> }, // 6x status codes
    )
}