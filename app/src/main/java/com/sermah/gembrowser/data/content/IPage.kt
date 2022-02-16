package com.sermah.gembrowser.data.content

interface IPage {
    var response: IResponse
    var data: PageData
    var content: List<ContentLine>
    var source: String
    var old: Boolean

    fun updateData()
}