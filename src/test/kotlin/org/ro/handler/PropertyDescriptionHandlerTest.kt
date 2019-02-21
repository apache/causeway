package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.core.Utils
import org.ro.core.event.EventLog
import org.ro.core.event.ListObserver
import org.ro.core.event.LogEntry
import org.ro.core.model.ObjectList
import org.ro.to.FR_PROPERTY_DESCRIPTION
import org.ro.to.Property
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ImplicitReflectionSerializer
class PropertyDescriptionHandlerTest {

    //  BS3.xml <-(link.layout)- FR <-(up)- FR_PROPERTY_DESCRIPTION
    @Test
    fun testService() {
        if (TestUtil().isSimpleAppAvailable()) {

            // given
            val lo = ListObserver()
            val jsonStr = FR_PROPERTY_DESCRIPTION.str
            // when
            val xp = LogEntry("", "GET")
            xp.response = jsonStr
            Dispatcher.handle(xp)
            val selfHref: String = Utils().getSelfHref(jsonStr)!!
            assertNotNull(selfHref)
            val act: LogEntry = EventLog.find(selfHref)!!

            val obs: ListObserver = act.observer as ListObserver
            val ol: ObjectList = obs.getList()
            // then
            assertNotNull(ol)
            assertTrue(xp == act)

            //val url:String = tObject.getLayoutLink() //"get URL from URLS.FR_PROPERTY_DESCRIPTION"
            // tObject.getLayoutLink
            // will only work, if it has been loaded ...
            val lyt = ol.getLayout()!!
            assertNotNull(lyt)

            val property = JSON.parse(Property.serializer(), FR_PROPERTY_DESCRIPTION.str)
            val id: String = "property.id"
            val lbl: String? = lyt.getPropertyLabel(id)
            assertNotNull(lbl)
        }
    }

}