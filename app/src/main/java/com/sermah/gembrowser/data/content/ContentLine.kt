package com.sermah.gembrowser.data.content

data class ContentLine(
    val data: String,
    val type: ContentType,
    val extra: String = ""
    ){

    enum class ContentType {
        TEXT,
        PRE,
        LINK,
        H1,
        H2,
        H3,
        UL,
        QUOTE,
        EMPTY,
    }
}