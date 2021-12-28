package com.sermah.gembrowser.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sermah.gembrowser.R
import com.sermah.gembrowser.data.StyleManager
import com.sermah.gembrowser.model.ContentLine
import com.sermah.gembrowser.view.LineView

class ContentAdapter(
    private val context: Context,
    private val lines: List<ContentLine>
) : RecyclerView.Adapter<ContentAdapter.ItemViewHolder>() {

    class ItemViewHolder(
        private val view: View,
    ) : RecyclerView.ViewHolder(view){
        val lineView : LineView = view.findViewById(R.id.content_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_content_line, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val line = lines[position]
        var text = line.data.trim()
        var style = StyleManager.styleText
        val lineView = holder.lineView

        //Log.d("Debug", line.data)

        lineView.data = line.data

        lineView.unlink()
        when (line.type) {
            ContentLine.ContentType.LINK -> {
                lineView.link(Uri.parse(line.extra))
                style = StyleManager.styleLink
            }
            ContentLine.ContentType.PRE -> {
                text = line.data // not trimmed
                style = StyleManager.stylePre
            }
            ContentLine.ContentType.H1 -> {
                style = StyleManager.styleH1
            }
            ContentLine.ContentType.H2 -> {
                style = StyleManager.styleH2
            }
            ContentLine.ContentType.H3 -> {
                style = StyleManager.styleH3
            }
            ContentLine.ContentType.QUOTE -> {
                style = StyleManager.styleQuote
            }
            ContentLine.ContentType.UL -> {
                style = StyleManager.styleUl // TODO: Leave original indents (multilevel lists)
            }
            ContentLine.ContentType.EMPTY -> {
                style = StyleManager.styleEmpty
            }
            else -> { // also TEXT
            }
        }
        // Apply style before setting text, because pre-/postfixes are put in setText
        lineView.applyStyle(style)
        lineView.setRawText(text)
    }

    override fun getItemCount() = lines.size
}