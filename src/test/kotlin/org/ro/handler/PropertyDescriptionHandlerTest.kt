package org.ro.handler

import kotlin.test.Test

class PropertyDescriptionHandlerTest {

    //  BS3.xml <-(link.layout)- FR <-(up)- FR_PROPERTY_DESCRIPTION
    @Test
    fun testService() {
        /*
        // given
        val log = EventLog
        val lo = ListObserver()
        // when
        val xp = LogEntry("", "GET", null)
        val json: JsonObject = URLS.FR_PROPERTY_DESCRIPTION
        xp.response = JSON.stringify(json)
        Dispatcher.handle(xp)
        val selfHref: String? = Utils().getSelfHref(json)
        val act: LogEntry? = log.find(selfHref)
        //FIXME nothing tested right now!
        if (act != null) {
            val obs: ListObserver = act.observer as ListObserver
            val ol: ObjectList = obs.getList()
            // then
            assertNotNull(ol)
            assertTrue(xp == act)

            //val url:String = tObject.getLayoutLink() //"get URL from URLS.FR_PROPERTY_DESCRIPTION"
            // tObject.getLayoutLink
            // will only work, if it has been loaded ...
            val lyt = ol.getLayout()
            assertNotNull(lyt)

            val id: String = URLS.FR_PROPERTY_DESCRIPTION.id
            val lbl: String = lyt.getPropertyLabel(id)
            assertNotNull(lbl)
        }
        */
    }

}