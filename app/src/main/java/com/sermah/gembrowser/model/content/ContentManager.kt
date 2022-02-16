package com.sermah.gembrowser.model.content

import android.net.Uri
import com.sermah.gembrowser.data.content.*
import com.sermah.gembrowser.model.network.GeminiClient
import com.sermah.gembrowser.model.parsing.GmiParser
import com.sermah.gembrowser.util.Utils
import com.sermah.gembrowser.viewmodel.BrowserActivityViewModel
import java.util.*

object ContentManager{

    const val CONTENT_HISTORY_SIZE = 4

    lateinit var browserActivityVM: BrowserActivityViewModel

    // TODO: Load homepage from storage
    var homepage: Uri = Uri.parse("gemini://gemini.circumlunar.space")

    var currentTab: Tab = Tab()
    var tabs: MutableList<Tab> = mutableListOf(currentTab)
    private var bookmarks: MutableList<String> = mutableListOf()

    fun requestUri(uri: Uri, replaceCurrent: Boolean = false) {
        if (uri.scheme != "gemini") browserActivityVM.modelIntentOutside(uri)
        else GeminiClient.connectAndRetrieve(
            uri = uri,
            onSuccess = { header, body ->
                val response = GeminiResponse(header, body)
                val pageData = PageData(uri = uri, isBookmark = bookmarks.contains(uri.toString())) // TODO: Update isBookmark
                val newPage = GeminiPage(response, pageData)

                browserActivityVM.modelShowPage(newPage)

                if (!replaceCurrent)
                    openPage(newPage)
                else {
                    currentTab.currentPage = newPage
                    updateVMHistory()
                }
            },
            onNotSuccess = { header ->
                if (header.length >= 2) {
                    val meta = if (header.length > 3) header.substring(3) else ""
                    val code = header.substring(0, 2)

                    when (code[0]) {
                        '1'  -> browserActivityVM.modelRequestQuery(meta, uri, code[1] == '1')
                        '3'  -> requestUri(Uri.parse(meta))
                        else -> browserActivityVM.modelShowUnsuccess(code, meta, uri)
                    }
                }
            },
        )
    }

    fun parseBody(body: String, meta: String, uri: Uri): List<ContentLine> {
        val lines: List<ContentLine> = when {
            meta.startsWith("text/gemini") -> {
                GmiParser.parse(body, uri)
            }
            meta.startsWith("text") -> {
                listOf(ContentLine(body, ContentLine.ContentType.PRE))
            }
            else -> {
                val sizeString =
                    Utils.byteSizeReadable(body.toByteArray(Charsets.UTF_8).size)
                listOf(
                    ContentLine("Resource of type", ContentLine.ContentType.H3),
                    ContentLine(meta.trim(), ContentLine.ContentType.H2),
                    ContentLine("", ContentLine.ContentType.EMPTY),
                    ContentLine("This file format is not supported yet...", ContentLine.ContentType.TEXT),
                )
            }
        }

        return lines
    }

    fun goBackward(): Boolean {
        val page = currentTab.historyTravel(1) ?: run {
            updateVMHistory()
            return false
        }
        currentTab.currentPage = page
        browserActivityVM.modelShowPage(page)
        updateVMHistory()

        return true
    }

    fun goForward(): Boolean {
        val page = currentTab.historyTravel(-1) ?: run {
            updateVMHistory()
            return false
        }
        currentTab.currentPage = page
        browserActivityVM.modelShowPage(page)
        updateVMHistory()

        return true
    }

    private fun openPage(page: IPage) {
        currentTab.openPage(page)
        currentTab.makePagesOld(CONTENT_HISTORY_SIZE)
        updateVMHistory()
    }

    fun addBookmark(uri: Uri) {
        // TODO: save bookmark
        if (uri.scheme != null && uri.authority != null && uri.isAbsolute)
            bookmarks.add(uri.toString())
        currentTab.currentPage.data.isBookmark = true
        browserActivityVM.modelUpdatePageData(currentTab.currentPage.data)
    }

    fun removeBookmark(uri: Uri) {
        bookmarks.remove(uri.toString())
        currentTab.currentPage.data.isBookmark = false
        browserActivityVM.modelUpdatePageData(currentTab.currentPage.data)
    }

    fun getBookmarks() : List<String> { return bookmarks }

    fun getCurrentTabHistory() : List<HistoryEntry> {
        return currentTab.history.map { HistoryEntry(it.data.title, Date(), it.data.uri) }
    }

    private fun updateVMHistory() {
        browserActivityVM.modelSetHistory(getCurrentTabHistory())
        browserActivityVM.modelSetHistoryIndex(currentTab.currentIndex)
    }
}