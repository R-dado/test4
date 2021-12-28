package com.sermah.gembrowser.model

import android.graphics.Color
import android.graphics.Typeface

data class LineStyle (
    var size        : Float     = 16f, // Sp
    var padTop      : Float     = 0f,  // Dp
    var padBottom   : Float     = 0f,  // Dp
    var padStart    : Float     = 16f, // Dp
    var padEnd      : Float     = 16f, // Dp
    var lineSpacing  : Float    = 0f, // Sp
    var color       : TextColor = TextColor(),
    var style       : TextStyle = TextStyle.NORMAL, // normal, bold, italic, bold_italic
    var align       : Alignment = Alignment.START,
    var typeface    : Typeface? = null,
    var prefix      : String    = "",
    var postfix     : String    = "",
    ) {

    enum class Alignment {
        START, CENTER, END
    }
    enum class TextStyle {
        NORMAL, BOLD, ITALIC, BOLD_ITALIC
    }

    class TextColor (
        val light: Int = Color.BLACK,
        val dark: Int = Color.WHITE
    )
}