package com.sermah.gembrowser.repository

import com.sermah.gembrowser.dataclasses.IBookmarksData
import com.sermah.gembrowser.dataclasses.IContentsData
import com.sermah.gembrowser.dataclasses.IHistoryData

interface IBrowsingRepository {

    fun getHistoryData(): IHistoryData
    fun getBookmarksData(): IBookmarksData
    fun getContentsData(): IContentsData

    // Returns homepage URL as String (can't be empty)
    fun getHomepage(): String

}