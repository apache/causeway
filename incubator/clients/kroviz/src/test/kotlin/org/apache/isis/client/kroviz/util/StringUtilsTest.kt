package org.apache.isis.client.kroviz.util

import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.StringUtils
import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilsTest {
    @Test
    fun testShortTitle() {
        // given
        UiManager.login(Constants.demoUrl, Constants.demoUser, Constants.demoPass)
        val url = "http://localhost:8080/restful/domain-types/demo.JavaLangStrings/collections/entities"

        // when
        val actual = StringUtils.shortTitle(url, UiManager.getUrl())

        // then
        val expected = "/domain-types/demo.JavaLangStrings/collections/entities"
        assertEquals(expected, actual)
    }

}
