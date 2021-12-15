package org.apache.isis.client.kroviz.util

import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.StringUtils
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

}
