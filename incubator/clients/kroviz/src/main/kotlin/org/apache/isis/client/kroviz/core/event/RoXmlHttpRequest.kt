/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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

    fun invokeAnonymous(link: Link, aggregator: BaseAggregator?, subType: String = Constants.subTypeXml) {
        val reSpec = ResourceSpecification(link.href)
        console.log("[RXHR.invokeAnonymous]")
        console.log(EventStore.isCached(reSpec, link.method))
        when {
            EventStore.isCached(reSpec, link.method) -> processCached(reSpec)
            else -> processAnonymous(link, aggregator, subType)
        }
    }

    private fun processAnonymous(link: Link, aggregator: BaseAggregator?, subType: String) {
        val method = link.method
        val url = link.href

        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Content-Type", Constants.stdMimeType)
        xhr.setRequestHeader("Accept", Constants.svgMimeType)

        val reSpec = ResourceSpecification(url, subType)
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
