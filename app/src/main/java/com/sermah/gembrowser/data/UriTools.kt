package com.sermah.gembrowser.data

import android.net.Uri

object UriTools {
    fun Uri.addRelative(rel: Uri) : Uri {
        if (rel.isAbsolute or this.isRelative) return this

        var resultPath = this.path ?: ""
        if (resultPath.endsWith('/')) resultPath = resultPath.removeSuffix("/")
        val relIsDirectory = rel.path?.endsWith('/') == true

        if (rel.path?.startsWith('/') == true)
            resultPath = ""
        else if (this.lastPathSegment?.contains('.') == true)
            resultPath = this.path?.substringBeforeLast('/') ?: ""

        rel.pathSegments.forEach {
            if (it == "..")
                resultPath = resultPath.substringBeforeLast('/')
            else if (it != ".")
                resultPath += "/$it"
        }

        if (relIsDirectory) resultPath += '/'
        return this.buildUpon().path(resultPath).build()
    }

    fun Uri.addRelative(rel: String) =
        this.addRelative(Uri.parse(rel))
}