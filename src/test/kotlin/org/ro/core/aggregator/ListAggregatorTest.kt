package org.ro.core.aggregator

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.core.event.EventStore
import org.ro.core.model.DisplayList
import org.ro.core.event.ResourceSpecification
import org.ro.snapshots.simpleapp1_16_0.*
import org.ro.to.Property
import org.ro.to.RelType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class ListAggregatorTest : IntegrationTest() {

    @Test
    fun testFixtureResult() {
        if (isAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListAggregator("test")
            // when
            mockResponse(FR_OBJECT, obs)
            mockResponse(FR_OBJECT_LAYOUT, obs)
            mockResponse(FR_OBJECT_PROPERTY, obs)
            val reSpec = ResourceSpecification(FR_OBJECT_PROPERTY.url)
            val pLe = EventStore.find(reSpec)!!
            val pdLe = mockResponse(FR_PROPERTY_DESCRIPTION, obs)
            val layoutLe = mockResponse(FR_OBJECT_LAYOUT, obs)

            val p = pLe.getTransferObject() as Property
            val links = p.links
            val descLink = links.find {
                it.rel == RelType.DESCRIBEDBY.type
            }
            val actObs = pLe.getAggregator() as ListAggregator
            val dl = obs.dsp
            val property = pdLe.getTransferObject() as Property
            val propertyLabels = (dl as DisplayList).propertyDescriptionList
            val lbl = propertyLabels.get(property.id)!!
            // then
            assertEquals("className", p.id) //1
            assertNotNull(descLink)                  //2
            assertEquals(obs, actObs)              //3
            assertNotNull(actObs.dsp.layout)         // 4
            assertEquals(pdLe.getAggregator(), layoutLe.getAggregator()) // 5
            assertNotNull(dl.layout) // 6
            assertTrue(propertyLabels.size > 0)   // 7
            assertEquals("ResultListResult class", lbl)
        }
    }

    @Test
    fun testService() {
        if (isAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListAggregator("test")
            // when
            mockResponse(SO_LIST_ALL, obs)
            mockResponse(SO_0, obs)
            // then
            val ol = obs.dsp
            assertNotNull(ol)
            assertEquals(1,  (ol as DisplayList).data.size)
        }
    }

}
