package org.ro.core.event

import org.ro.core.Session
import org.ro.handler.ResponseHandler
import org.ro.to.Link
import org.ro.to.Method
import org.w3c.xhr.XMLHttpRequest

/**
 * The name is somewhat misleading, see: https://en.wikipedia.org/wiki/XMLHttpRequest
 */

class RoXmlHttpRequest {
    private var url = ""

    protected fun errorHandler(responseText: String) {
        EventStore.fault(url, responseText)
    }

    protected fun resultHandler(responseText: String) {
        val jsonString: String = responseText
        val logEntry: LogEntry? = EventStore.end(url, jsonString)
        if (logEntry != null) {
            ResponseHandler.handle(logEntry)
        }
    }

    fun invoke(link: Link, obs: IObserver?) {
        //@kotlinx.coroutines.InternalCoroutinesApi cancel()
        url = link.href
        if (EventStore.isCached(url)) {
            EventStore.update(url)
        }
        val method = link.method
        val credentials: String = Session.getCredentials()
        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
        xhr.setRequestHeader("Accept", "application/json")

        xhr.onload = { _ -> resultHandler(xhr.responseText) }
        xhr.onerror = { _ -> errorHandler(xhr.responseText) }
        xhr.ontimeout = { _ -> errorHandler(xhr.responseText) }

        var body = ""
        if (method == Method.POST.operation) {
            body = link.argumentsAsBody()
            xhr.send(body)
        } else {
            xhr.send()
        }
        EventStore.start(url, method, body, obs)
    }

}
