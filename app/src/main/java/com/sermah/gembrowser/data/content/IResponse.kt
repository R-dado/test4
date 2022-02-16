package com.sermah.gembrowser.data.content

import android.text.SpannableString

interface IResponse {
    override fun toString(): String
    fun toFullString(): String
}