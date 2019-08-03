package org.ro.handler

import org.ro.core.event.NavigationObserver
import org.ro.to.Result
import org.ro.urls.RESTFUL_SERVICES
import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceHandlerTest : IntegrationTest() {

    @Test
    fun testService() {
        // given
        val observer = NavigationObserver()
        // when
        val logEntry = mockResponse(RESTFUL_SERVICES, observer)
        val result = logEntry.obj as Result
        val actual = result.value.size
        // then
        assertEquals(8, actual)
    }

}
