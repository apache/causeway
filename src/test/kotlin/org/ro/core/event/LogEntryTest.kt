package org.ro.core.event

import kotlinx.serialization.UnstableDefault
import kotlin.test.Test
import kotlin.test.assertTrue

@UnstableDefault
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
