package com.sermah.gembrowser.dataclasses

interface IHistoryData {

    val entries: Collection<IHistoryEntry>
    val current: IHistoryEntry

    val canForward: Boolean
    val canBackward: Boolean

}