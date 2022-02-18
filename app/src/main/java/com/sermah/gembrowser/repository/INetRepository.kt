package com.sermah.gembrowser.repository

import com.sermah.gembrowser.dataclasses.throwables.InvalidURLException
import kotlin.jvm.Throws

interface INetRepository {

    @Throws(InvalidURLException::class)
    fun requestUri()

}