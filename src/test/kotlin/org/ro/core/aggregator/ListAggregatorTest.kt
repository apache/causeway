package org.ro.core.aggregator

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.core.event.EventStore
import org.ro.to.Property
import org.ro.to.RelType
import org.ro.urls.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class ListAggregatorTest : IntegrationTest() {

    @Test
    fun testFixtureResult() {
        if (isSimpleAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListAggregator("test")
            // when
            mockResponse(FR_OBJECT, obs)
            mockResponse(FR_OBJECT_LAYOUT, obs)
            mockResponse(FR_OBJECT_PROPERTY, obs)
            val pLe = EventStore.find(FR_OBJECT_PROPERTY.url)!!
            val pdLe = mockResponse(FR_PROPERTY_DESCRIPTION, obs)
            val layoutLe = mockResponse(FR_OBJECT_LAYOUT, obs)

            val p = pLe.getObj() as Property
            val links = p.links
            val descLink = links.find {
                it.rel == RelType.DESCRIBEDBY.type
            }
            val actObs = pLe.aggregator as ListAggregator
            val dl = obs.list
            val property = pdLe.getObj() as Property
            val propertyLabels = dl.propertyLabels
            val lbl = dl.getPropertyLabel(property.id)!!
            // then
            assertEquals("className", p.id) //1
            assertNotNull(descLink)                  //2
            assertEquals(obs, actObs)              //3
            assertNotNull(actObs.list.layout)         // 4
            assertEquals(pdLe.aggregator, layoutLe.aggregator) // 5
            assertNotNull(dl.layout) // 6
            assertTrue(propertyLabels.size > 0)   // 7
            assertEquals("Result class", lbl)
        }
    }

    @Test
    fun testService() {
        if (isSimpleAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListAggregator("test")
            // when
            mockResponse(SO_LIST_ALL, obs)
            mockResponse(SO_0, obs)
            // then
            val ol = obs.list
            assertNotNull(ol)
            assertEquals(1, ol.getData().size)
        }
    }

}
