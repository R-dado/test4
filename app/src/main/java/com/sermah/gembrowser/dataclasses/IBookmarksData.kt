package com.sermah.gembrowser.dataclasses

interface IBookmarksData {

    val entries: List<IBookmark>

    fun isBookmark(uri: String): Boolean

}