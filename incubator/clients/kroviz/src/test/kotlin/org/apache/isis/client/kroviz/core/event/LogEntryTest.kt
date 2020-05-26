package org.apache.isis.client.kroviz.core.event

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogEntryTest {

    @Test
    fun testTitle() {
        // given
        val url = "https://kroki.io"

        // when
        val le = LogEntry(url)

        // then
        assertFalse(le.title.startsWith("/"))
    }

    @Test
    fun testCalculate() {
        // given
        val le = LogEntry("http://test/url")

        // when
        le.setSuccess()

        // then
        assertTrue(0 <= le.duration)

        if (le.duration < 0 && le.cacheHits == 0) {
            //TODO add assert
        }
    }

}
