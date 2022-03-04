package com.sermah.gembrowser.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.sermah.gembrowser.R
import com.sermah.gembrowser.dataclasses.IBottomButtonsStates
import com.sermah.gembrowser.dataclasses.IPaletteData
import com.sermah.gembrowser.dataclasses.PaletteColor

val UNKNOWN_COLOR = PaletteColor(Color.MAGENTA)

class BottomBarView (
    context: Context
) : LinearLayout(context) {

    private val titleView: TextView = TextView(context)
    private val inputField: EditText = EditText(context)
    private val recyclerView: RecyclerView = RecyclerView(context)
    private val buttons: Map<ButtonType, AppCompatImageButton> = mapOf()

    private var type = BarType.INPUT_URI
        set(v) {
            field = v
            when (v) {
                BarType.INPUT_QUERY, BarType.INPUT_URI -> becomeInput()
                BarType.LIST_CONTENTS, BarType.LIST_HISTORY -> becomeList()
            }
        }

    var inputValue = ""
        set(v){ field = v; inputField.setText(v) }
        get() { return inputField.text.toString() }

    var uriTitle = ""
        set(v){ field = v; updateTitle() }
    var queryTitle = ""
        set(v){ field = v; updateTitle() }

    var defaultUriTitle = ""
        set(v){ field = v; updateTitle() }
    var defaultQueryTitle = ""
        set(v){ field = v; updateTitle() }

    var contentsTitle = ""
        set(v){ field = v; updateTitle() }
    var historyTitle = ""
        set(v){ field = v; updateTitle() }

    var uriHint = ""
        set(v){ field = v; updateHint() }
    var queryHint = ""
        set(v){ field = v; updateHint() }

    private var onSendUriRequest: (String) -> Boolean = {true}
    private var onSendQueryResponse: (String) -> Boolean = {true}
    private var onBookmarkAdd: () -> Unit = {}
    private var onBookmarkRemove: () -> Unit = {}
    private var onHistoryBack: () -> Unit = {}
    private var onHistoryForward: () -> Unit = {}
    private var onHomepage: () -> Unit = {}
    private var onContents: () -> Unit = {}
    private var onOpenBookmarks: () -> Unit = {}
    private var onOpenHistory: () -> Unit = {}
    private var onMenu: () -> Unit = {} // TODO: This is probably not needed

    /////////////////////////////////////////////////////
    // Public setters for private values and other things

    // Gets values from palette and applies them to children
    fun setPalette(newPalette: IPaletteData) {
        newPalette.safeGet("backgroundBottom", UNKNOWN_COLOR).run {
            setBackgroundColor(this.value)
        }
        newPalette.safeGet("backgroundBottomInput", UNKNOWN_COLOR).run {
            inputField.setBackgroundColor(this.value)
        }
        newPalette.safeGet("iconNeutral", UNKNOWN_COLOR).run {
            buttons.forEach {
                it.value.imageTintList = ColorStateList.valueOf(this.value)
            }
        }
    }

    fun setButtonsStates(newStates: IBottomButtonsStates) {
        buttons.forEach {
            val b = it.value
            when (it.key) {
                ButtonType.HISTORY_BACK ->      b.isEnabled = newStates.backEnabled
                ButtonType.HISTORY_FORWARD ->   b.isEnabled = newStates.forwardEnabled
                ButtonType.CONTENTS ->          b.isEnabled = newStates.contentsEnabled
                ButtonType.BOOKMARK ->          {
                    b.setImageResource(
                        if (newStates.isBookmarked) R.drawable.ic_bookmark
                        else R.drawable.ic_bookmark_border
                    )
                    b.setOnClickListener(
                        if (newStates.isBookmarked) OnClickListener { bookmarkAdd() }
                        else OnClickListener { bookmarkRemove() }
                    )
                }
                else -> {}
            }
        }
    }

    fun setBarType(newType: BarType) {
        type = newType
    }

    fun setOnSendQueryResponse(toRun: (String) -> Boolean) {
        onSendQueryResponse = toRun
    }

    fun setOnSendUriRequest(toRun: (String) -> Boolean) {
        onSendUriRequest = toRun
    }

    fun setOnBookmarkAdd(toRun: () -> Unit) {
        onBookmarkAdd = toRun
    }

    fun setOnBookmarkRemove(toRun: () -> Unit) {
        onBookmarkRemove = toRun
    }

    fun setOnHomepage(toRun: () -> Unit) {
        onHomepage = toRun
    }

    fun setOnContents(toRun: () -> Unit) {
        onContents = toRun
    }

    fun setOnOpenBookmarks(toRun: () -> Unit) {
        onOpenBookmarks = toRun
    }

    fun setOnOpenHistory(toRun: () -> Unit) {
        onOpenHistory = toRun
    }

    //////////////////////////////////////////////
    // Buttons onClick & EditText onKey functions

    private fun sendQueryResponse() {
        if (onSendQueryResponse(inputValue))
            loseFocus()
    }

    private fun sendUriRequest() {
        if (onSendUriRequest(inputValue))
            loseFocus()
    }

    private fun bookmarkAdd() { onBookmarkAdd() }

    private fun bookmarkRemove() { onBookmarkRemove() }

    private fun historyBack() { onHistoryBack() }

    private fun historyForward() { onHistoryForward() }

    private fun openHomepage() { onHomepage() }

    private fun openContents() { onContents() }

    private fun openHistory() { onOpenHistory() }

    private fun openBookmarks() { onOpenBookmarks() }

    /////////////////////////////
    // Transformation functions

    private fun becomeInput() {
        inputField.post {
            visibility = View.GONE
        }
        recyclerView.post {
            visibility = View.VISIBLE
        }
        updateTitle()
        updateHint()
        updateOnKey()
    }

    private fun becomeList() {
        inputField.post {
            visibility = View.VISIBLE
        }
        recyclerView.post {
            visibility = View.GONE
        }
        updateTitle()
    }

    ///////////////////////
    // Value updaters

    private fun updateTitle() {
        titleView.text = when (type) {
            BarType.INPUT_URI -> uriTitle.ifBlank { defaultUriTitle }
            BarType.INPUT_QUERY -> queryTitle.ifBlank { defaultQueryTitle }
            BarType.LIST_CONTENTS -> contentsTitle
            BarType.LIST_HISTORY -> historyTitle
        }
    }

    private fun updateHint() {
        inputField.hint = when (type) {
            BarType.INPUT_URI -> uriHint
            BarType.INPUT_QUERY -> queryHint
            else -> ""
        }
    }

    private fun updateOnKey() {
        inputField.setOnKeyListener(
            when (type) {
                BarType.INPUT_URI ->    OnKeyListener{ v, k, e -> uriOnKey(v,k,e) }
                BarType.INPUT_QUERY ->  OnKeyListener{ v, k, e -> queryOnKey(v,k,e) }
                else -> OnKeyListener{ _,_,_ -> false }
            }
        )
    }

    ////////////////////////
    // onKeyListeners

    private fun uriOnKey(v: View, k: Int, e: KeyEvent): Boolean {
        if((e.action != KeyEvent.ACTION_DOWN) || (k != KeyEvent.KEYCODE_ENTER)) return false
        if(inputField.text.isNotBlank())
            sendUriRequest()
        return true
    }

    private fun queryOnKey(v: View, k: Int, e: KeyEvent): Boolean {
        if((e.action != KeyEvent.ACTION_DOWN) || (k != KeyEvent.KEYCODE_ENTER)) return false
        sendQueryResponse()
        return true
    }

    /////////////////////////

    private fun loseFocus() {
        inputField.clearFocus()
    }

    private fun setButtonsClickListeners() {
        buttons.forEach {
            val b = it.value
            b.setOnClickListener(
                when (it.key) {
                    ButtonType.HISTORY_BACK ->      OnClickListener { historyBack() }
                    ButtonType.HISTORY_FORWARD ->   OnClickListener { historyForward() }
                    ButtonType.HOMEPAGE ->          OnClickListener { openHomepage() }
                    ButtonType.CONTENTS ->          OnClickListener { openContents() }
                    else ->                         OnClickListener { }
                }
            )
            b.setOnLongClickListener(
                when (it.key) {
                    ButtonType.HISTORY_BACK ->      OnLongClickListener { openHistory(); true }
                    ButtonType.BOOKMARK ->          OnLongClickListener { openBookmarks(); true }
                    else ->                         OnLongClickListener { false }
                }
            )
        }
    }

    //////////////////////////
    // Enums

    enum class ButtonType {
        HISTORY_BACK, HISTORY_FORWARD, HOMEPAGE, CONTENTS, BOOKMARK, MENU
    }

    enum class BarType {
        INPUT_URI, INPUT_QUERY, LIST_CONTENTS, LIST_HISTORY
    }

}