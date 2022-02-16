package com.sermah.gembrowser.data.content

import android.net.Uri
import java.util.*

data class HistoryEntry (
    val title: String,
    val time: Date,
    val uri: Uri,
)