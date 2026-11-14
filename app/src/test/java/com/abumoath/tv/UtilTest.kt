package com.abumoath.tv

import com.abumoath.tv.data.source.SubtitleShift
import com.abumoath.tv.data.source.TextTools
import com.abumoath.tv.player.PlayerFactory
import com.abumoath.tv.util.Format
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilTest {
    @Test fun arabicNormalisation() {
        assertEquals("احمد الامارات", TextTools.normalize("أَحْمَد الإمارات"))
        assertEquals("مدرسه", TextTools.normalize("مدرسة"))
    }
    @Test fun fuzzySearch() {
        val q = "jurney"
        assertTrue(TextTools.score(TextTools.normalize(q), TextTools.tokens(q), TextTools.normalize("The Long Journey")) > 0)
        assertEquals(0, TextTools.score("xyz", listOf("xyz"), "al jazeera"))
    }
    @Test fun subtitleShift() {
        val vtt = SubtitleShift.toVtt("1\n00:00:01,000 --> 00:00:02,000\nHi\n", 1500)
        assertTrue(vtt.contains("00:00:02.500 --> 00:00:03.500"))
    }
    @Test fun timeFormatting() {
        assertEquals("01:05", Format.time(65_000))
        assertEquals("1:00:00", Format.time(3_600_000))
    }
    @Test fun languageMapping() { assertEquals("ar", PlayerFactory.lang2("ara")) }
}
