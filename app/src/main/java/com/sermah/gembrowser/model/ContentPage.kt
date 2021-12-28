package com.sermah.gembrowser.model

import android.net.Uri

data class ContentPage(
    val uri: Uri,
    val favicon: String = "🌐",
    val lines: List<ContentLine> = listOf(),
    val header: String,
    val body: String,
)