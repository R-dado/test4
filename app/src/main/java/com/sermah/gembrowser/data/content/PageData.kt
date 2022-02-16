package com.sermah.gembrowser.data.content

import android.net.Uri

data class PageData (
    var title: String = "", // First H1 or Uri host name
    var icon: String = "🌐", // Fetched from favicon.txt
    var uri: Uri,
    var isBookmark: Boolean = false,
    var mimeType: String = "text/*",
) {

    init {
        if (title.isBlank()) title = uri.lastPathSegment.toString()
        if (title.isBlank()) title = uri.host.toString()
    }

}