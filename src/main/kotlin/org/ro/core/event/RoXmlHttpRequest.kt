package org.ro.core.event

import org.ro.core.Session
import org.ro.core.aggregator.Aggregator
import org.ro.handler.ResponseHandler
import org.ro.to.Argument
import org.ro.to.Link
import org.ro.to.Method
import org.w3c.xhr.XMLHttpRequest

/**
 * The name is somewhat misleading, see: https://en.wikipedia.org/wiki/XMLHttpRequest
 */
class RoXmlHttpRequest {

    fun invoke(link: Link, aggregator: Aggregator?) {
        //@kotlinx.coroutines.InternalCoroutinesApi cancel()
        var url = link.href
        if (EventStore.isCached(url)) {
            EventStore.update(url)
        }
        val method = link.method
        val credentials: String = Session.getCredentials()
        if (method != Method.POST.operation) {
            url = url + argumentsAsUrlParameter(link)
        }

        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
        xhr.setRequestHeader("Accept", "application/json")

        xhr.onload = { _ -> resultHandler(url, xhr.responseText) }
        xhr.onerror = { _ -> errorHandler(url, xhr.responseText) }
        xhr.ontimeout = { _ -> errorHandler(url, xhr.responseText) }

        var body = ""
        if (link.hasArguments()) {
            body = argumentsAsBody(link)
            xhr.send(body)
        } else {
            xhr.send()
        }
        EventStore.start(url, method, body, aggregator)
    }

    private fun resultHandler(url: String, responseText: String) {
        val jsonString: String = responseText
        val logEntry: LogEntry? = EventStore.end(url, jsonString)
        if (logEntry != null) {
            ResponseHandler.handle(logEntry)
        }
    }

    private fun errorHandler(url: String, responseText: String) {
        EventStore.fault(url, responseText)
    }

    private fun argumentsAsBody(link: Link): String {
        val args = link.argMap()!!
        var body = "{"
        for (kv in args) {
            val arg = kv.value!!
            body = body + arg.asBody() + ","
        }
        val len = body.length
        body = body.replaceRange(len - 1, len, "}")
        return body
    }

    private fun argumentsAsUrlParameter(link: Link): String {
        val args = link.argMap()
        return argumentsAsString(args, "?", ",", "")
    }

    private fun argumentsAsString(
            args: Map<String, Argument?>?,
            start: String,
            sep: String,
            end: String): String {
        if (args.isNullOrEmpty()) {
            return ""
        } else {
            var answer = start
            args.forEach { kv ->
                val arg = kv.value!!
                answer = answer + arg.key + "=" + arg.value + sep  //IMPROVE define a function
            }
            val len = answer.length
            answer = answer.replaceRange(len - 1, len, end)
            return answer
        }
    }

}
