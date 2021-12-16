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

import org.apache.isis.client.kroviz.core.aggregator.AggregatorWithLayout
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.handler.ResponseHandler
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.utils.StringUtils
import org.apache.isis.client.kroviz.utils.UrlUtils
import org.w3c.xhr.BLOB
import org.w3c.xhr.TEXT
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

/**
 * The name is somewhat misleading, see: https://en.wikipedia.org/wiki/XMLHttpRequest
 */
class RoXmlHttpRequest(val aggregator: BaseAggregator?) {

    private val xhr = XMLHttpRequest()

    private val CONTENT_TYPE = "Content-Type"
    private val ACCEPT = "Accept"

    internal fun process(link: Link, subType: String) {
        val method = link.method
        var url = link.href
        if (method != Method.POST.operation) {
            url += StringUtils.argumentsAsUrlParameter(link)
        }
        val credentials: String = SessionManager.getCredentials()!!

        xhr.open(method, url, true)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader(CONTENT_TYPE, "application/$subType;charset=UTF-8")
        xhr.setRequestHeader(ACCEPT, "application/$subType, ${Constants.pngMimeType}")
        if (UrlUtils.isIcon(url)) {
            xhr.responseType = XMLHttpRequestResponseType.BLOB
        }

        val body = buildBody(link)
        val rs = buildResourceSpecificationAndSetupHandler(url, subType, body)

        when {
            body.isEmpty() -> xhr.send()
            else -> xhr.send(body)
        }
        SessionManager.getEventStore().start(rs, method, body, aggregator)
    }

    private fun buildBody(link: Link): String {
        return when {
            link.hasArguments() -> StringUtils.argumentsAsBody(link)
            link.method == Method.PUT.operation -> {
                val logEntry = SessionManager.getEventStore().findBy(aggregator!!)
                when (val obj = logEntry?.obj) {
                    is TObject -> StringUtils.propertiesAsBody(obj)
                    else -> ""
                }
            }
            else -> ""
        }
    }

    internal fun processNonREST(link: Link, subType: String) {
        val method = link.method
        val url = link.href

        xhr.open(method, url, true)
        xhr.setRequestHeader(CONTENT_TYPE, Constants.stdMimeType)
        xhr.setRequestHeader(ACCEPT, Constants.svgMimeType)

        val body = StringUtils.argumentsAsList(link)
        xhr.send(body)
        val rs = buildResourceSpecificationAndSetupHandler(url, subType, body)

        SessionManager.getEventStore().start(rs, method, body, aggregator)
    }

    internal fun invokeKroki(pumlCode: String) {
        val method = Method.POST.operation
        val url = Constants.krokiUrl + "plantuml"

        xhr.open(method, url, true)
        xhr.setRequestHeader(CONTENT_TYPE, Constants.stdMimeType)
        xhr.setRequestHeader(ACCEPT, Constants.svgMimeType)

        val rs = buildResourceSpecificationAndSetupHandler(url, Constants.subTypeJson, pumlCode)

        xhr.send(pumlCode)
        SessionManager.getEventStore().start(rs, method, pumlCode, aggregator)
    }

    private fun buildResourceSpecificationAndSetupHandler(
        url: String,
        subType: String,
        body: String
    ): ResourceSpecification {
        val rs = ResourceSpecification(url, subType)
        xhr.onload = { _ -> handleResult(rs, body) }
        xhr.onerror = { _ -> handleError(rs) }
        xhr.ontimeout = { _ -> handleError(rs) }
        return rs
    }

    private fun handleResult(rs: ResourceSpecification, body: String) {
        val response: Any? = xhr.response
        val le: LogEntry? = SessionManager.getEventStore().end(rs, body, response)
        if (le != null) {
            when {
                aggregator == null -> ResponseHandler.handle(le)
                le.obj == null -> ResponseHandler.handle(le)
                aggregator is AggregatorWithLayout -> aggregator.update(le, le.subType)
                else -> ResponseHandler.handle(le)
            }
        }
    }

    private fun handleError(rs: ResourceSpecification) {
        val error = when (xhr.responseType) {
            XMLHttpRequestResponseType.BLOB -> "blob error"
            XMLHttpRequestResponseType.TEXT -> xhr.responseText
            else -> "neither text nor blob"
        }
        SessionManager.getEventStore().fault(rs, error)
    }

}
