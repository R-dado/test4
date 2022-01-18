package com.sermah.gembrowser

import android.net.Uri
import com.sermah.gembrowser.util.UriTools.addRelative
import org.junit.Test

import org.junit.Assert.*

class UriToolsTest {
    @Test
    fun addRelative_isCorrect() {
        val map = mapOf<String, Uri>( // expected result to function result
            "https://test.com/rel" to Uri.parse("https://test.com/").addRelative("/rel"),
            "https://test.com/rel" to Uri.parse("https://test.com/").addRelative("rel"),
            "https://test.com/dir/rel" to Uri.parse("https://test.com/dir/").addRelative("rel"),
            "https://test.com/rel" to Uri.parse("https://test.com/file.txt").addRelative("rel"),
            "https://test.com/rel" to Uri.parse("https://test.com/dir/").addRelative("/rel"),
            "https://test.com/rel" to Uri.parse("https://test.com/dir/file.txt").addRelative("/rel"),
            "https://test.com/dir/rel" to Uri.parse("https://test.com/dir/file.txt").addRelative("rel"),
            "https://test.com/dir/rel" to Uri.parse("https://test.com/dir").addRelative("rel"),
            )

        map.forEach {
            assertEquals(it.key, it.value.toString())
        }
    }
}