package org.ro.urls

import org.ro.core.Session
import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.to.*
import pl.treksoft.kvision.utils.Object
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This is an integration test that requires SimpleApp running on http://localhost:8080
 *
 * @item Iterate over all Objects defined in package 'urls',
 * @item check if the href 'self' can be invoked and
 * @item compare the response of invoking 'self' with what is hardcoded
 */
class UrlsTest {

    var link: Link? = null;
    var urls = mutableMapOf<String, String>()

    @BeforeTest
    fun setUp() {
        Session.login("http://localhost:8080", "sven", "pass")
        urls.put(FR_OBJECT.url, FR_OBJECT.str)
        urls.put(FR_OBJECT_BAZ.url, FR_OBJECT_BAZ.str)
        urls.put(FR_OBJECT_LAYOUT.url, FR_OBJECT_LAYOUT.str)
        urls.put(FR_OBJECT_PROPERTY.url, FR_OBJECT_PROPERTY.str)
        urls.put(FR_PROPERTY_DESCRIPTION.url, FR_PROPERTY_DESCRIPTION.str)
        urls.put(SO_0.url, SO_0.str)
        urls.put(SO_LIST_ALL.url, SO_LIST_ALL.str)
        urls.put(SO_LIST_ALL_INVOKE.url, SO_LIST_ALL_INVOKE.str)
        urls.put(SO_LIST_ALL_OBJECTS.url, SO_LIST_ALL_OBJECTS.str)
        urls.put(SO_MENU.url, SO_MENU.str)
        urls.put(SO_OBJECT_LAYOUT.url, SO_OBJECT_LAYOUT.str)
        urls.put(RESTFUL_SERVICES.url, RESTFUL_SERVICES.str)
    }

    @AfterTest
    fun tearDown() {
        /*
        if (timer) {
            timer.stop()
        }
        timer = null */
    }


    @Test
    fun testUrls() {
        for (entry in urls) {
            val href = entry.key
            val link = Link(method = Method.GET.operation, href = href)
            /*
            val observed = RoXmlHttpRequest().invoke(link)
            assertEquals(entry.value, observed)   */
        }
        /*
val asyncHandler: Function = Async.asyncHandler(this, handleLinkComplete, 500, null, handleTimeout)
timer.addEventListener(TimerEvent.TIMER_COMPLETE, asyncHandler, false, 0, true)
timer.start() */
    }

    fun handleLinkComplete(passThroughData: Object) {
        val href: String = link!!.href
        val logEntry: LogEntry = EventStore.find(href)!!
        val resp: String = logEntry.retrieveResponse()
        //TODO handle authentication problem: logEntry.fault="Security error accessing url"
        //  http://localhost:8080/crossdomain.xml
        if (logEntry == null) {
            val observed: Object = JSON.parse(resp)
            //pass over String to Services
            val expected: Object? = null
            assertEquals(expected, observed)
        } else {
            console.log("[Fault: ${logEntry}]")
        }
    }
}
