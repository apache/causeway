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
import org.apache.isis.client.kroviz.core.aggregator.SvgDispatcher
import org.apache.isis.client.kroviz.handler.ResponseHandler
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.StringUtils
import org.w3c.xhr.BLOB
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

/**
 * The name is somewhat misleading, see: https://en.wikipedia.org/wiki/XMLHttpRequest
 */
class RoXmlHttpRequest {

    private val CONTENT_TYPE = "Content-Type"
    private val ACCEPT = "Accept"

    internal fun process(link: Link, aggregator: BaseAggregator?, subType: String) {
        val method = link.method
        var url = link.href
        if (method != Method.POST.operation) {
            url += StringUtils.argumentsAsUrlParameter(link)
        }
        val credentials: String = UiManager.getCredentials()

        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader(CONTENT_TYPE, "application/$subType;charset=UTF-8")
        xhr.setRequestHeader(ACCEPT, "application/$subType, ${Constants.pngMimeType}")
        if (url.endsWith("object-icon")) {
            xhr.responseType = XMLHttpRequestResponseType.BLOB
        }

        val body = buildBody(link, aggregator)
        val rs = buildResourceSpecificationAndSetupHandler(url, subType, body, xhr)

        when {
            body.isEmpty() -> xhr.send()
            else -> xhr.send(body)
        }
        EventStore.start(rs, method, body, aggregator)
    }

    private fun buildBody(link: Link, aggregator: BaseAggregator?): String {
        return when {
            link.hasArguments() -> StringUtils.argumentsAsBody(link)
            link.method == Method.PUT.operation -> {
                val logEntry = EventStore.findBy(aggregator!!)
                when (val obj = logEntry?.obj) {
                    is TObject -> StringUtils.propertiesAsBody(obj)
                    else -> ""
                }
            }
            else -> ""
        }
    }

    internal fun processNonREST(link: Link, aggregator: BaseAggregator?, subType: String) {
        val method = link.method
        val url = link.href

        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader(CONTENT_TYPE, Constants.stdMimeType)
        xhr.setRequestHeader(ACCEPT, Constants.svgMimeType)

        val body = StringUtils.argumentsAsList(link)
        xhr.send(body)
        val rs = buildResourceSpecificationAndSetupHandler(url, subType, body, xhr)

        EventStore.start(rs, method, body, aggregator)
    }

    internal fun invokeKroki(pumlCode: String, agr: SvgDispatcher) {
        val method = Method.POST.operation
        val url = Constants.krokiUrl + "plantuml"

        val xhr = XMLHttpRequest()
        xhr.open(method, url, true)
        xhr.setRequestHeader(CONTENT_TYPE, Constants.stdMimeType)
        xhr.setRequestHeader(ACCEPT, Constants.svgMimeType)

        val rs = buildResourceSpecificationAndSetupHandler(url, Constants.subTypeJson, pumlCode, xhr)

        xhr.send(pumlCode)
        EventStore.start(rs, method, pumlCode, agr)
    }

    private fun buildResourceSpecificationAndSetupHandler(
            url: String,
            subType: String,
            body: String,
            xhr: XMLHttpRequest): ResourceSpecification {
        val rs = ResourceSpecification(url, subType)
        xhr.onload = { _ -> handleResult(rs, body, xhr) }
        xhr.onerror = { _ -> handleError(rs, xhr) }
        xhr.ontimeout = { _ -> handleError(rs, xhr) }
        return rs
    }

    private fun handleResult(rs: ResourceSpecification, body: String, xhr: XMLHttpRequest) {
        val response:Any? = xhr.response
        val logEntry: LogEntry? = EventStore.end(rs, body, response)
        if (logEntry != null) ResponseHandler.handle(logEntry)
    }

    private fun handleError(rs: ResourceSpecification, xhr: XMLHttpRequest) {
        val responseText = xhr.responseText
        EventStore.fault(rs, responseText)
    }

}
