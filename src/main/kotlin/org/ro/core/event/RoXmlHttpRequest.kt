package org.ro.core.event

import org.ro.core.Session
import org.ro.handler.Dispatcher
import org.ro.to.Link
import org.ro.to.Method
import org.w3c.xhr.XMLHttpRequest

/**
 * The name is somewhat misleading, see: https://en.wikipedia.org/wiki/XMLHttpRequest
 */
class RoXmlHttpRequest {
    private var url = ""

    protected fun errorHandler(responseText: String) {
        EventLog.fault(url, responseText)
    }

    protected fun resultHandler(responseText: String) {
        val jsonString: String = responseText
        val logEntry: LogEntry? = EventLog.end(url, jsonString)
        if (logEntry != null) {
            Dispatcher.handle(logEntry)
        }
    }

    fun invoke(link: Link, obs: ILogEventObserver?) {
        //@kotlinx.coroutines.InternalCoroutinesApi cancel()
        url = link.href
        if (EventLog.isCached(url)) {
            EventLog.update(url)
        }
        val method = link.method
        val credentials: String = Session.getCredentials()
        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
        xhr.setRequestHeader("Accept", "application/json")

        xhr.onload = { event -> resultHandler(xhr.responseText) }
        xhr.onerror = { event -> errorHandler(xhr.responseText) }
        xhr.ontimeout = { event -> errorHandler(xhr.responseText) }

        var body = ""
        if (method == Method.POST.operation) {
            body = link.getArgumentsAsJsonString()
            console.log("[RoXHR.POST body: $body]")
            xhr.send(body)
        } else {
            xhr.send()
        }
        EventLog.start(url, method, body, obs)
    }

}