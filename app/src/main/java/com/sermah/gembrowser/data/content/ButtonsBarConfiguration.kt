package com.sermah.gembrowser.data.content

data class ButtonsBarConfiguration (
    val canBack: Boolean,
    val canForward: Boolean,
    val isBookmark: Boolean,
    val hasContents: Boolean,
) {

    // Returns copy with changed values
    fun with(
        canBack: Boolean = this.canBack,
        canForward: Boolean = this.canForward,
        isBookmark: Boolean = this.isBookmark,
        hasContents: Boolean = this.hasContents,
    ): ButtonsBarConfiguration {
        return ButtonsBarConfiguration(
            canBack,
            canForward,
            isBookmark,
            hasContents,
        )
    }
}