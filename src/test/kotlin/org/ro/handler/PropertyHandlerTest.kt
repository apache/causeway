package org.ro.handler

import org.ro.core.event.EventLog
import org.ro.core.event.ListObserver
import org.ro.core.event.LogEntry
import org.ro.to.*
import kotlin.test.Test
import kotlin.test.assertNotNull

class PropertyHandlerTest {

    @Test
    fun testService() {
        if (TestUtil().isSimpleAppAvailable()) {
            // given
            val file = FR_OBJECT_PROPERTY
            val str = file.str
            val url = file.url
            val method = Method.GET.operation
            val layout = LayoutHandler().parse(FR_OBJECT_LAYOUT.str)
            val layoutLe = LogEntry(FR_OBJECT_LAYOUT.url)
            layoutLe.obj = layout
            val obs = ListObserver()
            obs.update(layoutLe)

            // when 
        //    val le = EventLog.start(url, method, obs = obs)
        //    EventLog.end(url, str)
        //    Dispatcher.handle(le)
        //    console.log("[PHT.testService] PH.handle: ${le.observer}")
            val link = Link(url, method)
            link.invoke(obs)
            // then 
            val actLe: LogEntry? = EventLog.find(url)
            console.log("[PHT.testService] EL.find $actLe")
            console.log("[PHT.testService] ${actLe!!.observer}")
            assertNotNull(actLe)  //1
            assertNotNull(actLe.obj)  //2
            val p = actLe.obj as Property
            console.log("[PHT.testService] $p")  
            assertNotNull(p.id)    // 3
            assertNotNull(p.descriptionLink())  //4
        }
    }

}