package com.sermah.gembrowser.view

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import com.sermah.gembrowser.R
import com.sermah.gembrowser.data.content.ContentManager
import com.sermah.gembrowser.data.theming.StyleManager.dpToPx
import com.sermah.gembrowser.model.content.ContentLine
import com.sermah.gembrowser.model.theming.LineStyle

import android.content.ClipData
import android.content.ClipboardManager
import android.view.Gravity
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu


class LineView: AppCompatTextView {
    var data: String = ""
    var lineType: ContentLine.ContentType = ContentLine.ContentType.TEXT
    private var rawText: String = ""
    private var prefix: String = ""
    private var postfix: String = ""

    var horizontalScroll: Boolean = false
    set(b) {
        if (b) enableHorizontalScroll()
        else disableHorizontalScroll()
        field = b
    }
    
    init {
        setOnTouchListener { _, event ->
            if (lineType == ContentLine.ContentType.LINK)
            this.alpha =  when(event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_MOVE -> 0.7f
                else -> 1.0f
            }
            if (horizontalScroll && (
                        event.action == MotionEvent.ACTION_MOVE &&
                        event.historySize > 1 &&
                        (event.x - event.getHistoricalX(1)) > (event.y - event.getHistoricalY(1))
                    )) {
                parent.requestDisallowInterceptTouchEvent(true)
            }
            super.onTouchEvent(event)
        }
    }

    // Raw text is needed, because if pre/postfix concat is in setText,
    // then TextView may occasionally wrap its text into another pair of pre/postfixes
    // when updating itself and calling setText on itself
    fun setRawText(text: String) {
        rawText = text
        this.text = prefix + rawText + postfix
    }

    fun link(uri: Uri){
        setOnClickListener {
            ContentManager.requestUri(uri)
        }
        setOnLongClickListener { // TODO: Make a dialog for link - link address, copy link, open link in separate tab
            val menu = PopupMenu(context, this, Gravity.END, 0, R.style.Widget_GemBrowser_PopupMenu)

            menu.menu.add(1, Menu.NONE, Menu.NONE, uri.toString())
                .setOnMenuItemClickListener {
                    Toast.makeText(context, uri.toString(), Toast.LENGTH_SHORT).show()
                    true
                }
            menu.menu.add(2, Menu.NONE, Menu.NONE, context.getString(R.string.menu_link_copy))
                .setOnMenuItemClickListener {
                    val clipboard: ClipboardManager? =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newRawUri(context.getString(R.string.clipboard_label_link), uri)
                    clipboard?.setPrimaryClip(clip)
                    true
                }
            menu.menu.add(2, Menu.NONE, Menu.NONE, context.getString(R.string.menu_link_copy_text))
                .setOnMenuItemClickListener {
                    val clipboard: ClipboardManager? =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText(context.getString(R.string.clipboard_label_link), rawText)
                    clipboard?.setPrimaryClip(clip)
                    true
                }
            menu.show()
            true
        }

        setTextIsSelectable(false)
        isClickable = true
        isLongClickable = true
        isFocusable = true
        //paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    fun unlink() {
        setOnClickListener(null)
        setOnLongClickListener(null)
        setTextIsSelectable(true)
        isClickable = false
        isLongClickable = false
        isFocusable = true
        //paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
    }

    fun enableHorizontalScroll() {
        setHorizontallyScrolling(true)
        isHorizontalFadingEdgeEnabled = true
    }

    fun disableHorizontalScroll() {
        setHorizontallyScrolling(false)
        isHorizontalFadingEdgeEnabled = false
    }

    fun applyStyle(style: LineStyle) {
        prefix = style.prefix
        postfix = style.postfix

        textAlignment = when (style.align) {
            LineStyle.Alignment.START ->
                TEXT_ALIGNMENT_VIEW_START
            LineStyle.Alignment.CENTER ->
                TEXT_ALIGNMENT_CENTER
            LineStyle.Alignment.END ->
                TEXT_ALIGNMENT_VIEW_END
        }
        setTypeface(style.typeface, when (style.style) {
            LineStyle.TextStyle.NORMAL      ->       Typeface.NORMAL
            LineStyle.TextStyle.BOLD        ->         Typeface.BOLD
            LineStyle.TextStyle.ITALIC      ->       Typeface.ITALIC
            LineStyle.TextStyle.BOLD_ITALIC ->  Typeface.BOLD_ITALIC
        })
        setTextSize(TypedValue.COMPLEX_UNIT_SP, style.size)
        setTextColor(style.color)
        setPaddingRelative(
            dpToPx(this.context, style.padStart),
            dpToPx(this.context, style.padTop),
            dpToPx(this.context, style.padEnd),
            dpToPx(this.context, style.padBottom),
        )
        setLineSpacing(dpToPx(this.context, style.lineSpacing).toFloat(), 1f)
        horizontalScroll = style.nowrap
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)
}