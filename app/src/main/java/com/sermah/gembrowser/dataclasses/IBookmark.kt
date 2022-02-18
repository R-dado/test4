package com.sermah.gembrowser.dataclasses

interface IBookmark {

    val title: String
    val uri: String

    fun equals(): Boolean

}