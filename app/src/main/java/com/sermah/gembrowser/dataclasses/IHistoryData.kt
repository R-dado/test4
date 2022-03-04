package com.sermah.gembrowser.dataclasses

interface IHistoryData {

    val entries: List<IHistoryEntry>
    val current: IHistoryEntry

    val canForward: Boolean
    val canBackward: Boolean

}