package com.sermah.gembrowser.viewmodel

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sermah.gembrowser.data.content.*
import com.sermah.gembrowser.model.content.ContentManager
import java.lang.StringBuilder

class BrowserActivityViewModel : ViewModel() {

    init {
        ContentManager.browserActivityVM = this
        viewOpenHomepage()
    }

    private val _bbState: MutableLiveData<BottomBarState> = MutableLiveData(BottomBarState.TITLE)
    val bbState: LiveData<BottomBarState> get() = _bbState

    private val _bbTitle: MutableLiveData<String> = MutableLiveData("")
    val bbTitle: LiveData<String> get() = _bbTitle

    private val _bbUriInput: MutableLiveData<String> = MutableLiveData("")
    val bbUriInput: LiveData<String> get() = _bbUriInput

    private val _bbQueryPrompt: MutableLiveData<String> = MutableLiveData("")
    val bbQueryPrompt: LiveData<String> get() = _bbQueryPrompt

    private val _bbQueryInput: MutableLiveData<String> = MutableLiveData("")
    val bbQueryInput: LiveData<String> get() = _bbQueryInput

    private val _bbButtonsConfig: MutableLiveData<ButtonsBarConfiguration> = MutableLiveData()
    val bbButtonsConfig: LiveData<ButtonsBarConfiguration> get() = _bbButtonsConfig


    private val _pageData: MutableLiveData<PageData> = MutableLiveData(PageData(uri = Uri.EMPTY))
    val pageData: LiveData<PageData> get() = _pageData

    private val _page: MutableLiveData<IPage> = MutableLiveData()
    val page: LiveData<IPage> get() = _page

    private val _history: MutableLiveData<List<HistoryEntry>> = MutableLiveData(listOf())
    val history: LiveData<List<HistoryEntry>> get() = _history

    private val _historyCurrentIndex: MutableLiveData<Int> = MutableLiveData(0)
    val historyCurrentIndex: LiveData<Int> get() = _historyCurrentIndex

    private val _bookmarks: MutableLiveData<List<String>> = MutableLiveData(listOf())
    val bookmarks: LiveData<List<String>> get() = _bookmarks

    private val _unansweredQuery: MutableLiveData<Uri> = MutableLiveData(Uri.EMPTY)
    val unansweredQuery: LiveData<Uri> get() = _unansweredQuery

    private val _intentUri: MutableLiveData<Uri> = MutableLiveData()
    val intentUri: LiveData<Uri> get() = _intentUri

    private val _warningMessage: MutableLiveData<String> = MutableLiveData()
    val warningMessage: LiveData<String> get() = _warningMessage

    // View calls methods with `view` prefix - these methods probably call model methods
    // Model calls methods with `model` prefix - these methods probably call view methods

    //////////////////////////////////
    // View

    fun viewRequestUri(uri: Uri) { toModelRequestUri(uri) }
    fun viewRequestUri(str: String) {
        if (str.isBlank()) return
        toModelRequestUri(Uri.parse(str.run {
            if (!str.contains("://")) "gemini://$this"
            else this
        }))
    }

    fun viewGoForward() { toModelGoForward() }

    fun viewGoBackward() { toModelGoBackward() }

    fun viewAddBookmark() {
        if (_pageData.value != null)
            toModelAddBookmark(_pageData.value!!.uri)
    }

    fun viewDeleteBookmark() {
        if (_pageData.value != null)
            toModelRemoveBookmark(_pageData.value!!.uri)
    }

    fun viewSendQuery(str: String) {
        if (_unansweredQuery.value == null) return
        toModelRequestUri(
            _unansweredQuery.value!!
                .buildUpon().query(str).build()
        )
        _bbState.value = BottomBarState.TITLE
    }

    fun viewOpenHomepage() {
        toModelRequestUri(ContentManager.homepage)
    }

    fun viewShowContents()  { _bbState.value = BottomBarState.CONTENTS }
    fun viewShowHistory()   { _bbState.value = BottomBarState.HISTORY }
    fun viewShowBookmarks() { _bbState.value = BottomBarState.BOOKMARKS }
    fun viewOpenUriInput()  { _bbState.value = BottomBarState.URI_INPUT }

    //////////////////////////////////
    // Model

    fun modelShowPage(p: IPage) {
        _page.postValue(p)
        _pageData.postValue(p.data)
        updateButtonsConfig(p.data.isBookmark)
        _bbState.postValue(BottomBarState.TITLE)
        _bbTitle.postValue(p.data.icon + " " + p.data.title)
        _bbUriInput.postValue(p.data.uri.toString())
        logHistory()
    }

    fun modelSetHistory(entries: List<HistoryEntry>){
        _history.postValue(entries)
        updateButtonsConfig()
    }

    fun modelUpdatePageData(pd: PageData) {
        _pageData.postValue(pd)
        _bbTitle.postValue(pd.icon + " " + pd.title)
        updateButtonsConfig(pd.isBookmark)
    }

    fun modelShowUnsuccess(code: String, cause: String, uri: Uri) {
        _warningMessage.postValue("Code ${code}. $cause")
        // TODO: Show message
    }

    fun modelRequestQuery(prompt: String, uri: Uri, isPassword: Boolean) {
        _bbState.postValue(BottomBarState.QUERY_INPUT)
        _unansweredQuery.postValue(uri)
        _bbQueryPrompt.postValue(prompt)
    }

    fun modelIntentOutside(uri: Uri) {
        _intentUri.postValue(uri)
    }

    fun modelSetHistoryIndex(idx: Int) {
        _historyCurrentIndex.postValue(idx)
        updateButtonsConfig()
    }

    //////////////////////////////////////////////////////////
    // inner methods to request/send data from/to Model

    private fun toModelRequestHistory() {
        _history.value = ContentManager.getCurrentTabHistory()
    }

    private fun toModelRequestUri(uri: Uri): Boolean {
        if (!checkUri(uri)) return false
        ContentManager.requestUri( ensureHasScheme(uri) )
        return true
    }

    private fun toModelAddBookmark(uri: Uri): Boolean {
        if (!checkUri(uri)) return false
        ContentManager.addBookmark( ensureHasScheme(uri) )
        return true
    }

    private fun toModelRemoveBookmark(uri: Uri): Boolean {
        if (!checkUri(uri)) return false
        ContentManager.removeBookmark( ensureHasScheme(uri) )
        return true
    }

    private fun toModelSetHomepage(uri: Uri): Boolean {
        if (!checkUri(uri)) return false
        ContentManager.homepage = ensureHasScheme(uri)
        return true
    }

    private fun toModelGoBackward() {
        ContentManager.goBackward()
        logHistory()
    }

    private fun toModelGoForward() {
        ContentManager.goForward()
    }

    /////////////////////////
    // Other

    private fun updateButtonsConfig(isBookmark: Boolean? = null) {
        _bbButtonsConfig.postValue(
            _bbButtonsConfig.value?.copy(
                canForward = _history.value != null && _historyCurrentIndex.value!! < _history.value!!.size - 1,
                canBack = _history.value != null && _historyCurrentIndex.value!! > 0,
                isBookmark = isBookmark ?: _bbButtonsConfig.value?.isBookmark ?: false // it doesn't matter after second ?:
            )?:
            ButtonsBarConfiguration(
                canForward = _history.value != null && _historyCurrentIndex.value!! < _history.value!!.size - 1,
                canBack = _history.value != null && _historyCurrentIndex.value!! > 0,
                isBookmark = isBookmark ?: _pageData.value?.isBookmark?: false, // we assume it's false
                hasContents = false,
            )
        )
    }

    private fun checkUri(uri: Uri): Boolean {
        return uri.isHierarchical && uri.host != null
    }

    private fun ensureHasScheme(uri: Uri): Uri {
        return if (uri.scheme == null)
            uri.buildUpon()
                .scheme("gemini")
                .build()
        else uri
    }

    enum class BottomBarState {
        TITLE,
        URI_INPUT,
        QUERY_INPUT,
        BOOKMARKS,
        HISTORY,
        CONTENTS
    }

    private fun logHistory() {
        Log.d("logHistory", "H ${StringBuilder().run{history.value?.forEach { this.append("${it.uri}\n") } ?: return@run "null"; this.toString()}}, Idx ${historyCurrentIndex.value ?: "null"}")
    }
}