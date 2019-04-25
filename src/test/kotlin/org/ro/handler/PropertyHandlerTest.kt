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
            assertNotNull(actLe.obj)  //2
            val p = actLe.obj as Property
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
            val obs = ListObserver()
            // when
            val pdLe = mockResponse(FR_PROPERTY_DESCRIPTION, obs)
            assertNotNull(pdLe)  //(1)
            assertNotNull(pdLe.observer) //(2)

            val layoutLe = mockResponse(FR_OBJECT_LAYOUT, obs)
            assertNotNull(layoutLe)  //(3)
            assertEquals(pdLe.observer, layoutLe.observer) //(4)

            val ol = obs.list
            val lyt = ol.layout
            assertNotNull(lyt)  // 5

            val property = pdLe.obj as Property
            assertNotNull(property)  //(6)

            console.log("[PHT.testPropertyDescription] ${pdLe.toString()}")
            val props = ol.propertyLabels
            assertNotNull(props) //7
            console.log("[PHT.testPropertyDescription] $props")
            
            val lbl: String? = ol.getPropertyLabel(property.id)
            assertNotNull(lbl)  //(8)
        }
    }


}