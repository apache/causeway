package org.ro.handler

import org.ro.core.event.EventLog
import org.ro.core.event.LogEntry
import org.ro.core.model.ObjectList
import org.ro.to.FR_OBJECT_LAYOUT
import org.ro.to.FR_PROPERTY_DESCRIPTION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PropertyDescriptionHandlerTest {

    //  BS3.xml <-(link.layout)- FR <-(up)- FR_PROPERTY_DESCRIPTION
    @Test
    fun testService() {
        if (TestUtil().isSimpleAppAvailable()) {
            // given
            val str = FR_PROPERTY_DESCRIPTION.str
            val url = FR_PROPERTY_DESCRIPTION.url
            // when
            val le = LogEntry(url, "GET")
            val obs = le.initListObserver()
            le.response = str
            EventLog.start(url, method = "GET", obs = obs)
            EventLog.end(url, str)
            Dispatcher.handle(le)
            val act: LogEntry? = EventLog.find(url)
            assertNotNull(act)  //(1)
            assertNotNull(act.observer) //(2)

            val ol: ObjectList = obs.list
            // then
            assertNotNull(ol)  //(3)
            assertEquals(le.toString(), act.toString())  //(4)

            val layoutUrl = FR_OBJECT_LAYOUT.url  // real URL is not contained as 'up' in .str
            EventLog.add(layoutUrl)
            val layoutLE = EventLog.end(layoutUrl, FR_OBJECT_LAYOUT.str)
            assertNotNull(layoutLE)  //(5)
            Dispatcher.handle(layoutLE)
            obs.update(layoutLE)
            val lyt = ol.getLayout()
            assertNotNull(lyt)  //(6)

            val property = PropertyHandler().parse(FR_PROPERTY_DESCRIPTION.str)
            assertNotNull(property)  //(7)
            
            console.log("[PDHT.testService] ${lyt.propertyLabels}")
            val id: String = "property.id"
            val lbl: String? = lyt.getPropertyLabel(id)
            assertNotNull(lbl)  //(8)
        }
    }

}