package com.sermah.gembrowser.dataclasses

interface IBookmarksData {

    val entries: Collection<IBookmark>

    fun isBookmark(uri: String): Boolean

}