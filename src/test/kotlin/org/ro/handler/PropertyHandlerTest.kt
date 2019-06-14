package org.ro.handler

import org.ro.core.Utils
import org.ro.core.event.EventStore
import org.ro.core.event.ListObserver
import org.ro.core.event.LogEntry
import org.ro.to.*
import org.ro.urls.FR_OBJECT_PROPERTY_
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PropertyHandlerTest : IntegrationTest() {

    @Test
    fun testProperty() {
        if (isSimpleAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListObserver()
            // when
            mockResponse(FR_OBJECT_PROPERTY, obs)
            mockResponse(FR_OBJECT_LAYOUT, obs)
            // then 
            val actLe: LogEntry? = EventStore.find(FR_OBJECT_PROPERTY.url)
            assertNotNull(actLe)  //1
            val p = actLe.getObj()
            assertNotNull(p)  //2
            console.log("[[WTF]]")
            Utils.debug(p)
//            assertTrue(p is TObject)
            assertTrue(p is Property)
            assertNotNull(p.id)    // 3
            val links = p.links
            val descLink =  links.find {
                it.rel == RelType.DESCRIBEDBY.type
            }
            assertNotNull(descLink)  //4

            val actObs = actLe.observer as ListObserver
            assertEquals(obs, actObs)              //5
            assertNotNull(actObs.list.layout)         //6
        }
    }

//FIXME    @Test
    fun testObjectProperty() {
        if (isSimpleAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListObserver()
            // when
            mockResponse(FR_OBJECT_PROPERTY_, obs)
            // then 
            val actLe: LogEntry = EventStore.find(FR_OBJECT_PROPERTY_.url)!!
            assertNotNull(actLe.getObj())  //1
            val p = actLe.getObj() as Property
            assertNotNull(p.id)    // 2
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
            console.log("[PHT.testPropertyDescription] $property")

            console.log("[PHT.testPropertyDescription] ${pdLe.toString()}")
            val props = ol.propertyLabels
            assertNotNull(props) //5
            console.log("[PHT.testPropertyDescription] $props")
            
            ol.initPropertyDescription()
            assertNotNull(property.id)  //6
            val lbl: String? = ol.getPropertyLabel(property.id)
//FIXME            assertNotNull(lbl)  //7
        }
    }


}