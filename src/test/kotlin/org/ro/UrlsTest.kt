package org.ro


/**
 * This is an integration test that requires SimpleApp running on http://localhost:8080
 *
 * @item Iterate over all Objects defined in class URLS,
 * @item check if the href 'self' can be invoked and
 * @item compare the response of invoking 'self' with what is hardcoded
 */
class UrlsTest {
    /*
    private var timer: Timer
    private var link: Link
    private var


            @kotlin.test.BeforeTest

    fun setUp() {
        timer = Timer(100, 1)
        Session.login("http://localhost:8080", "sven", "pass")
        /*        urls = {}
                addUrl(URLS.FR_OBJECT)
                addUrl(URLS.FR_OBJECT_BAZ)
                addUrl(URLS.FR_OBJECT_LAYOUT)
                addUrl(URLS.FR_OBJECT_PROPERTY)
                addUrl(URLS.FR_PROPERTY_DESCRIPTION)
                addUrl(URLS.SO_0)
                addUrl(URLS.SO_LIST_ALL)
                addUrl(URLS.SO_LIST_ALL_INVOKE)
                addUrl(URLS.SO_LIST_ALL_OBJECTS)
                addUrl(URLS.SO_MENU)
                addUrl(URLS.SO_OBJECT_LAYOUT)
                addUrl(URLS.RESTFUL_SERVICES) */
    }

    @AfterTest
    fun tearDown() {
        if (timer) {
            timer.stop()
        }
        timer = null
    }


    @Test
    fun testUrl1() {
        object = URLS.RESTFUL_SERVICES
        var href: String = Utils().getSelfHref(object)
        initLink(href)

        var asyncHandler: Function = Async.asyncHandler(this, handleLinkComplete, 500, null, handleTimeout)
        timer.addEventListener(TimerEvent.TIMER_COMPLETE, asyncHandler, false, 0, true)
        timer.start()
    }

    private fun initLink(href: String): void {
        link = Link()
        link.setHref(href)
        link.setMethod(Invokeable.GET)
        link.invoke()
    }

    fun handleLinkComplete(event: TimerEvent, passThroughData: Object): void {
        var href: String = link.href
        var logEntry: LogEntry = Eventlog.find(href)
        var resp: String = logEntry.retrieveResponse()
        //TODO handle authentication problem: logEntry.fault="Security error accessing url"
        //  http://localhost:8080/crossdomain.xml
        if (logEntry.fault == null) {
            var observed: Object = JSON.parse(resp)
            //pass over String to Services
            var expected: Object = object
            var b: Boolean = Utils().areEqual(expected, observed)
            assertTrue(b) } else {
            trace(logEntry.fault)
        }
    }
   */
}
