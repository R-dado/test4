package com.sermah.gembrowser.dataclasses.throwables

class UnknownColorException(private val colorKey: String) : Exception() {
    override val message: String
        get() = "Color not found: $colorKey! " + super.message
}