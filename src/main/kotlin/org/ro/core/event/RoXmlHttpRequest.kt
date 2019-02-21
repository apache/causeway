package org.ro.core.event

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.core.Session
import org.ro.handler.Dispatcher
import org.ro.to.Invokeable
import org.ro.to.Link
import org.ro.to.Method
import org.w3c.xhr.XMLHttpRequest

/**
 * The name is somewhat misleading, see: https://en.wikipedia.org/wiki/XMLHttpRequest
 */
@ImplicitReflectionSerializer
class RoXmlHttpRequest {
    private var xhr = XMLHttpRequest()
    private var url = ""

    protected fun errorHandler(responseText: String) {
        EventLog.fault(url, responseText)
    }

    protected fun resultHandler(responseText: String) {
        val jsonString: String = responseText
        console.log(responseText);
        val logEntry: LogEntry? = EventLog.end(url, jsonString)
        if (logEntry != null) {
            Dispatcher.handle(logEntry)
        }
    }

    fun invoke(inv: Invokeable, obs: ILogEventObserver?): Unit {
        //@kotlinx.coroutines.InternalCoroutinesApi cancel()
        url = inv.href
        if (EventLog.isCached(url)) {
            EventLog.update(url)
        }
        val method = inv.method
        val credentials: String = Session.getCredentials()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
        xhr.setRequestHeader("Accept", "application/json")

        xhr.onload = { event -> resultHandler(xhr.responseText) }
        xhr.onerror = { event -> errorHandler(xhr.responseText) }
        xhr.ontimeout = { event -> errorHandler(xhr.responseText) }

        var body = ""
        if (method == Method.POST.operation) {
            //FIXME
            val l: Link = inv as Link
            body = l.getArgumentsAsJsonString()
            xhr.send(body)
        } else {
            xhr.send()
        }
        EventLog.start(url, method, body, obs)
    }

}