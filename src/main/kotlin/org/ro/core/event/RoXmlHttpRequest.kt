package org.ro.core.event

import org.ro.core.Globals
import org.ro.to.Invokeable
import org.ro.to.Link
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest

/**
 * The name is somewhat misleading, see: https://en.wikipedia.org/wiki/XMLHttpRequest
 */
class RoXmlHttpRequest {
    private var xhr = XMLHttpRequest()
    var url = ""

    init {
//        super.useProxy = false
//        addEventListener(FaultEvent.FAULT, xhrFaultHandler)
//        addEventListener(ResultEvent.RESULT, xhrResultHandler)
    }

    protected fun errorHandler(event: Event, responseText: String) {
        EventLog.fault(url, responseText)
    }

    protected fun resultHandler(event: Event, responseText: String) {
        val jsonString: String = responseText
        val logEntry: LogEntry? = EventLog.end(url, jsonString)
        if (logEntry != null) {
            Globals.dispatcher.handle(logEntry)
        }
    }

    fun invoke(inv: Invokeable, obs: ILogEventObserver?): Unit {
        //@kotlinx.coroutines.InternalCoroutinesApi cancel()
        url = inv.href
        if (EventLog.isCached(url)) {
            EventLog.update(url)
        }
        val method = inv.method
        val credentials: String = Globals.session.getCredentials()
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/jsoncharset=UTF-8")
        xhr.setRequestHeader("Accept", "application/json")

        xhr.open(method, url, true)
        xhr.onload = { event -> resultHandler(event, xhr.responseText) }
        xhr.onerror = { event -> errorHandler(event, xhr.responseText) }
        xhr.ontimeout = { event -> errorHandler(event, xhr.responseText) }

        var body = ""
        if (method == Invokeable().POST) {
            var l: Link = inv as Link
            body = l.getArgumentsAsJsonString()
            xhr.send(body)
        } else {
            xhr.send()
        }
        EventLog.start(url, method, body, obs)
    }

}