package com.sermah.gembrowser.dataclasses.throwables

class InvalidURLException(private val url: String) : Exception() {
    override val message: String
        get() = "Wrong URL format: \"${url}\"! " + super.message
}