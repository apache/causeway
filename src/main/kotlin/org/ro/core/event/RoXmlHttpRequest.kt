package org.ro.core.event

import org.ro.utils.Utils
import org.ro.core.aggregator.BaseAggregator
import org.ro.handler.ResponseHandler
import org.ro.to.Link
import org.ro.to.Method
import org.ro.ui.kv.UiManager
import org.w3c.xhr.XMLHttpRequest

/**
 * The name is somewhat misleading, see: https://en.wikipedia.org/wiki/XMLHttpRequest
 */
class RoXmlHttpRequest {

    fun invoke(link: Link, aggregator: BaseAggregator?) {
        val url = link.href
        if (EventStore.isCached(url, link.method)) {
            processCached(url)
        } else {
            process(link, aggregator)
        }
    }

    private fun processCached(url: String) {
        val le = EventStore.find(url)!!
        le.retrieveResponse()
        ResponseHandler.handle(le)
        EventStore.cached(url)
    }

    private fun process(link: Link, aggregator: BaseAggregator?) {
        val method = link.method
        var url = link.href
        if (method != Method.POST.operation) {
            url += Utils.argumentsAsUrlParameter(link)
        }
        val credentials: String = UiManager.getCredentials()

        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
        xhr.setRequestHeader("Accept", "application/json")

        xhr.onload = { _ -> resultHandler(url, xhr.responseText) }
        xhr.onerror = { _ -> errorHandler(url, xhr.responseText) }
        xhr.ontimeout = { _ -> errorHandler(url, xhr.responseText) }

        var body = ""
        when {
            link.hasArguments()
            -> body = Utils.argumentsAsBody(link)
            link.method == Method.PUT.operation -> {
                val tObject = aggregator?.getObject()!!
                body = Utils.propertiesAsBody(tObject)
            }
            else -> {
            }
        }
        if (body.isEmpty()) {
            xhr.send()
        } else {
            xhr.send(body)
        }
        EventStore.start(url, method, body, aggregator)
    }

    fun processAnonymous(link: Link, aggregator: BaseAggregator?) {
        val method = link.method
        val url = link.href

        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Content-Type", "text/plain")
        xhr.setRequestHeader("Accept", "image/svg+xml")

        xhr.onload = { _ -> resultHandler(url, xhr.responseText) }
        xhr.onerror = { _ -> errorHandler(url, xhr.responseText) }
        xhr.ontimeout = { _ -> errorHandler(url, xhr.responseText) }

        val body = Utils.argumentsAsList(link)
        xhr.send(body)
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

}
