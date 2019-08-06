package org.ro.handler

import kotlinx.serialization.UnstableDefault
import org.ro.core.event.ListObserver
import org.ro.core.model.ObjectList
import org.ro.to.SO_LIST_ALL_INVOKE
import kotlin.test.Test
import kotlin.test.assertNotNull

@UnstableDefault
class ResultListHandlerTest : IntegrationTest() {

    @Test
    fun testHandle() {
        if (isSimpleAppAvailable()) {
            // given
            val obs = ListObserver()
            // when
            mockResponse(SO_LIST_ALL_INVOKE, obs)
            // then
            val list: ObjectList = obs.list
            assertNotNull(list)
        }
    }

}
