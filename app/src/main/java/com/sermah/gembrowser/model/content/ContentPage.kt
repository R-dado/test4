package com.sermah.gembrowser.model.content

import android.net.Uri

data class ContentPage(
    val uri: Uri,
    val favicon: String = "🌐",
    val header: String,
    var body: String,
    var lines: List<ContentLine> = listOf(),
) {
    var tooOld = false
}