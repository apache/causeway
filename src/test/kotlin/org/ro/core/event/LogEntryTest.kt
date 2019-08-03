package org.ro.core.event

import kotlin.test.Test
import kotlin.test.assertTrue

class LogEntryTest {

    @Test
    fun testCalculate() {
        // given
        val le = LogEntry("http://test/url")

        // when
        le.setSuccess()

        // then
        assertTrue(0 <= le.duration)
    }

}
