package org.apache.isis.client.kroviz.core.event

import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.handler.ResponseHandler
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.kv.Constants
import org.apache.isis.client.kroviz.ui.kv.UiManager
import org.apache.isis.client.kroviz.utils.Utils
import org.w3c.xhr.XMLHttpRequest

/**
 * The name is somewhat misleading, see: https://en.wikipedia.org/wiki/XMLHttpRequest
 */
class RoXmlHttpRequest {

    fun invoke(link: Link, aggregator: BaseAggregator?, subType: String = Constants.subTypeJson) {
        val reSpec = ResourceSpecification(link.href)
        when {
            EventStore.isCached(reSpec, link.method) -> processCached(reSpec)
            else -> process(link, aggregator, subType)
        }
    }

    private fun processCached(reSpec: ResourceSpecification) {
        val le = EventStore.find(reSpec)!!
        le.retrieveResponse()
        ResponseHandler.handle(le)
        EventStore.cached(reSpec)
    }

    private fun process(link: Link, aggregator: BaseAggregator?, subType: String) {
        val method = link.method
        var url = link.href
        if (method != Method.POST.operation) {
            url += Utils.argumentsAsUrlParameter(link)
        }
        val credentials: String = UiManager.getCredentials()

        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/$subType;charset=UTF-8")
        xhr.setRequestHeader("Accept", "application/$subType")

        val reSpec = ResourceSpecification(url, subType)
        xhr.onload = { _ -> resultHandler(reSpec, xhr.responseText) }
        xhr.onerror = { _ -> errorHandler(reSpec, xhr.responseText) }
        xhr.ontimeout = { _ -> errorHandler(reSpec, xhr.responseText) }

        var body = ""
        when {
            link.hasArguments() -> body = Utils.argumentsAsBody(link)
            link.method == Method.PUT.operation -> {
                val logEntry = EventStore.findBy(aggregator!!)
                val obj = logEntry?.obj
                when (obj) {
                    is TObject -> body = Utils.propertiesAsBody(obj)
                    else -> {
                    }
                }
            }
            else -> {
            }
        }
        when {
            body.isEmpty() -> xhr.send()
            else -> xhr.send(body)
        }
        EventStore.start(reSpec, method, body, aggregator)
    }

    fun processAnonymous(link: Link, aggregator: BaseAggregator?) {
        val method = link.method
        val url = link.href

        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Content-Type", Constants.format)
        xhr.setRequestHeader("Accept", "image/svg+xml")

        val reSpec = ResourceSpecification(url, Constants.subTypeXml)
        xhr.onload = { _ -> resultHandler(reSpec, xhr.responseText) }
        xhr.onerror = { _ -> errorHandler(reSpec, xhr.responseText) }
        xhr.ontimeout = { _ -> errorHandler(reSpec, xhr.responseText) }

        val body = Utils.argumentsAsList(link)
        xhr.send(body)
        EventStore.start(reSpec, method, body, aggregator)
    }

    private fun resultHandler(reSpec: ResourceSpecification, responseText: String) {
        val logEntry: LogEntry? = EventStore.end(reSpec, responseText)
        if (logEntry != null) ResponseHandler.handle(logEntry)
    }

    private fun errorHandler(reSpec: ResourceSpecification, responseText: String) {
        EventStore.fault(reSpec, responseText)
    }

}
