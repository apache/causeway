package org.ro.handler

import org.ro.core.event.EventStore
import org.ro.core.event.ListObserver
import org.ro.core.event.LogEntry
import org.ro.to.FR_OBJECT_LAYOUT
import org.ro.to.FR_OBJECT_PROPERTY
import org.ro.to.FR_PROPERTY_DESCRIPTION
import org.ro.to.Property
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PropertyHandlerTest : IntegrationTest() {

    @Test
    fun testProperty() {
        if (isSimpleAppAvailable()) {
            // given
            val obs = ListObserver()
            // when
            val propLe = mockResponse(FR_OBJECT_PROPERTY, obs)
            val layoutLe = mockResponse(FR_OBJECT_LAYOUT, obs)
            // then 
            val actLe: LogEntry? = EventStore.find(FR_OBJECT_PROPERTY.url)
            assertNotNull(actLe)  //1
            assertNotNull(actLe.getObj())  //2
            val p = actLe.getObj() as Property
            assertNotNull(p.id)    // 3
            assertNotNull(p.descriptionLink())  //4

            val actObs = actLe.observer as ListObserver
            assertEquals(obs, actObs)              //5
            assertNotNull(actObs.list.layout)         //6
        }
    }

    @Test
    fun testPropertyDescription() {
        if (isSimpleAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListObserver()
            // when
            val pdLe = mockResponse(FR_PROPERTY_DESCRIPTION, obs)
            assertNotNull(pdLe.observer) //(1)

            val layoutLe = mockResponse(FR_OBJECT_LAYOUT, obs)
            assertEquals(pdLe.observer, layoutLe.observer) //(2)

            val ol = obs.list
            val lyt = ol.layout
            assertNotNull(lyt)  //3

            val property = pdLe.getObj() as Property
            assertNotNull(property)  //4

            console.log("[PHT.testPropertyDescription] ${pdLe.toString()}")
            val props = ol.propertyLabels
            assertNotNull(props) //5
            console.log("[PHT.testPropertyDescription] $props")
            
            val lbl: String? = ol.getPropertyLabel(property.id)
            assertNotNull(lbl)  //6
        }
    }


}