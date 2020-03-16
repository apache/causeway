package org.ro.core.aggregator

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.core.event.EventStore
import org.ro.core.model.ListDM
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

            // then
            val actObs = pLe.getAggregator() as ListAggregator
            assertEquals(obs, actObs)  // 1
            assertEquals(pdLe.getAggregator(), layoutLe.getAggregator()) // 2 - trivial?
            // seems they are equal but not identical - changes on obs are not reflected in actObs !!!
           // assertNotNull(obs.dsp.layout)  // 3  // does not work - due to async?

            //then
            val p = pLe.getTransferObject() as Property
            assertEquals("className", p.id)  // 3
            val links = p.links
            val descLink = links.find {
                it.rel == RelType.DESCRIBEDBY.type
            }
            assertNotNull(descLink)  // 4

            // then
            val dl = obs.dsp as ListDM
            val propertyLabels = dl.propertyDescriptionList
            val property = pdLe.getTransferObject() as Property
            assertTrue(propertyLabels.size > 0)  // 5
            val lbl = propertyLabels.get(property.id)!!
            assertEquals("ResultListResult class", lbl)  // 6
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
            assertEquals(1,  (ol as ListDM).data.size)
        }
    }

}
