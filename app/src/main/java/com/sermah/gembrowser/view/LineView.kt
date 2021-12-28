package com.sermah.gembrowser.view

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.sermah.gembrowser.data.ContentManager
import com.sermah.gembrowser.data.StyleManager
import com.sermah.gembrowser.data.StyleManager.dpToPx
import com.sermah.gembrowser.model.LineStyle

class LineView: AppCompatTextView {
    var data: String = ""
    private var rawText: String = ""
    private var prefix: String = ""
    private var postfix: String = ""

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
            Toast.makeText(context, uri.toString(), Toast.LENGTH_SHORT).show()
            true
        }

        setTextIsSelectable(false)
        isClickable = true
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    fun unlink() {
        setOnClickListener(null)
        setOnLongClickListener(null)
        setTextIsSelectable(true)
        isClickable = false
        paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
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
        setTextColor(if (StyleManager.isDark) style.color.dark else style.color.light)
        setPaddingRelative(
            dpToPx(this.context, style.padStart),
            dpToPx(this.context, style.padTop),
            dpToPx(this.context, style.padEnd),
            dpToPx(this.context, style.padBottom),
        )
        setLineSpacing(dpToPx(this.context, style.lineSpacing).toFloat(), 1f)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)
}