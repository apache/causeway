package org.ro.handler

import org.ro.core.Menu
import org.ro.urls.RESTFUL_SERVICES
import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceHandlerTest : IntegrationTest() {

    @Test
    fun testService() {
        if (isSimpleAppAvailable()) {
            // given
            // when
            mockResponse(RESTFUL_SERVICES, null)
            // then 
            assertEquals(8, Menu.limit)
        }
    }

}