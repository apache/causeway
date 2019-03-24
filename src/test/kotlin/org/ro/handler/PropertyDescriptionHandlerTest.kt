package org.ro.handler

import org.ro.core.event.EventLog
import org.ro.core.event.ListObserver
import org.ro.core.event.LogEntry
import org.ro.core.model.ObjectList
import org.ro.to.FR_PROPERTY_DESCRIPTION
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PropertyDescriptionHandlerTest {

    //  BS3.xml <-(link.layout)- FR <-(up)- FR_PROPERTY_DESCRIPTION
    @Test
    fun testService() {
        if (TestUtil().isSimpleAppAvailable()) {

            // given
            TestUtil().invokeFixtureScript()
            val str = FR_PROPERTY_DESCRIPTION.str
            val url = FR_PROPERTY_DESCRIPTION.url
            // when
            val xp = LogEntry(url, "GET")
            xp.response = str
            //EventLog.add(xp)
            Dispatcher.handle(xp)
            val act: LogEntry? = EventLog.find(url)
            assertNotNull(act)
            assertNotNull(act.observer)

            val obs: ListObserver = act.observer as ListObserver
            val ol: ObjectList = obs.list
            // then
            assertNotNull(ol)
            assertTrue(xp == act)

            //val url:String = tObject.getLayoutLink() //"get URL from URLS.FR_PROPERTY_DESCRIPTION"
            // tObject.getLayoutLink
            // will only work, if it has been loaded ...
            val lyt = ol.getLayout()!!
            assertNotNull(lyt)

            val property = PropertyHandler().parse(FR_PROPERTY_DESCRIPTION.str)
            val id: String = "property.id"
            val lbl: String? = lyt.getPropertyLabel(id)
            assertNotNull(lbl)
        }
    }

}