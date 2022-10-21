package org.apache.causeway.client.kroviz.util

import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.ui.core.SessionManager
import org.apache.causeway.client.kroviz.utils.StringUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringUtilsTest {
    @Test
    fun testShortTitle() {
        // given
        SessionManager.login(Constants.demoUrl, Constants.demoUser, Constants.demoPass)
        val url = "http://localhost:8080/restful/domain-types/demo.JavaLangStrings/collections/entities"

        // when
        val protocolHostPort = SessionManager.getBaseUrl()!!
        // then
        assertTrue(protocolHostPort.startsWith("http://"))

        // when
        val actual = StringUtils.shortTitle(url)
        // then
        val expected = "/domain-types/demo.JavaLangStrings/collections/entities"
        assertEquals(expected, actual)
    }

    @Test
    fun testFormat() {
        // given
        val int = 123456789
        // when
        val actual = StringUtils.format(int)
        // then
        val expected = "123.456.789"
        assertEquals(expected, actual)
    }
}
