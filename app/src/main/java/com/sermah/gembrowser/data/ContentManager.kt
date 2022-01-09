package com.sermah.gembrowser.data

import android.net.Uri
import com.sermah.gembrowser.data.network.GeminiClient
import com.sermah.gembrowser.data.parsing.GmiParser
import com.sermah.gembrowser.model.ContentLine
import com.sermah.gembrowser.model.ContentPage

object ContentManager {

    const val CONTENT_HISTORY_SIZE = 4

    var currentPage: ContentPage = ContentPage(uri = Uri.parse("/"), header = "", body = "")
    var contentUpdateHandler: ContentUpdateHandler = ContentUpdateHandler()
    var onNonGeminiScheme: (Uri) -> Unit = {}
    var pagesHistory: MutableList<ContentPage> = mutableListOf()

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
                if (currentPage.lines.isNotEmpty()) pushToHistory(currentPage)
                currentPage = ContentPage(uri = uri, lines = lines, header = header, body = body)
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
        val page = popFromHistory() ?: return false
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
        currentPage = page
        contentUpdateHandler.onSuccess(meta, lines)

        return true
    }

    fun pushToHistory(page: ContentPage) {
        pagesHistory.add(page)
        if (pagesHistory.size > CONTENT_HISTORY_SIZE) {
            val change = pagesHistory[pagesHistory.size - CONTENT_HISTORY_SIZE]
            pagesHistory[pagesHistory.size - CONTENT_HISTORY_SIZE] = ContentPage(
                change.uri, change.favicon, listOf(), change.header, change.body
            )
        }
    }

    fun popFromHistory(): ContentPage? {
        return if (pagesHistory.size > 0) pagesHistory.removeLast() else null
    }

    fun getCachedVersion(): ContentPage? {
        return null
    }

    fun getTestContent(): List<ContentLine> {
        return GmiParser.parse(
            """
            # Header 1
            
            Sample text
            Sexy line (Gem)
            ```
            ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
            ░░░░     ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
            ▒▒  ▒▒▒▒   ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
            ▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒   ▒▒▒▒▒    ▒   ▒   ▒▒
            ▓   ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓   ▓▓▓   ▓▓  ▓▓   ▓
            ▓   ▓▓▓      ▓         ▓▓▓   ▓▓  ▓▓   ▓
            ▓▓   ▓▓▓▓  ▓▓▓  ▓▓▓▓▓▓▓▓▓▓   ▓▓  ▓▓   ▓
            ███      ███████     ████    ██  ██   █
            ███████████████████████████████████████
            ```
            
            > Quote! This man was Albert Einstein
            > - Jason Statham
            
            => https://google.com Google.com
            => https://yandex.ru Yandex.ru
            => https://yahoo.com Yahoo.com
            
            Sample text 2
            
            ## Header 2
            
            Long long long sentence(no).
            And the sentence right after it.
            
            This is why:
            * I like trains
            * I like pizza
            * I hate mushrooms and people
            
            Space between this and list
            
            * List item
            
            Multilevel lists:
            * Level 1
            *   Level 2
            *     Level 3
            
            ### H3 and pre after
            
            ``` secret msg
            pre pre {
                indented
                    meow
                quack
            }
            ```
            Pre mustve ended here
            
            ### Link code
            
            That's from ContentAdapter.kt:
            ```
            ContentLine.ContentType.LINK -> {
                val link = text.substringBefore(' ').trim()
                text = text.substringAfter(' ').trim() // TODO: Link prefix option, link address postfix option

                lineView.link(Uri.parse(link))

                style = StyleManager.styleLink
            }
            ```
            """.trimIndent(), Uri.parse("gemini://localhost/")
        )
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