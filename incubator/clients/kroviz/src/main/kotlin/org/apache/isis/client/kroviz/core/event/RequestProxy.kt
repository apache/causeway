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
import org.apache.isis.client.kroviz.ui.core.Constants

/**
 * Facade for RoXmlHttpRequest. If a request is being issued, it:
 *
 * * looks in EventStore, if a (similar) request has been issued before and can be retrieved from local storage, and if
 * 1. No: issue the real request and handle the response via ResponseHandler (chain)
 * 2. Yes: use the cached response and pass it directly to the respective Aggregator/Dispatcher
 *
 */
class RequestProxy {

    fun invoke(link: Link, aggregator: BaseAggregator? = null, subType: String = Constants.subTypeJson) {
        val rs = ResourceSpecification(link.href)
        when {
            isCached(rs, link.method) -> processCached(rs)
            else -> RoXmlHttpRequest().process(link, aggregator, subType)
        }
    }

    private fun processCached(rs: ResourceSpecification) {
        val le = EventStore.findBy(rs)!!
        le.retrieveResponse()
        ResponseHandler.handle(le)
        cached(rs)
    }

    fun cached(reSpec: ResourceSpecification): LogEntry {
        val entry: LogEntry = EventStore.findBy(reSpec)!!
        entry.setCached()
        EventStore.updateStatus(entry)
        return entry
    }

    fun invokeNonREST(link: Link, aggregator: BaseAggregator?, subType: String = Constants.subTypeXml) {
        val rs = ResourceSpecification(link.href)
        when {
            isCached(rs, link.method) -> processCached(rs)
            else -> RoXmlHttpRequest().processNonREST(link, aggregator, subType)
        }
    }

    private fun isCached(reSpec: ResourceSpecification, method: String): Boolean {
        val le = EventStore.findBy(reSpec)
        return when {
            le == null -> false
            le.hasResponse() && le.method == method && le.subType == reSpec.subType -> {
                le.setCached()
                true
            }
            le.isView() -> true
            else -> false
        }
    }

    fun invokeKroki(pumlCode: String, agr: SvgDispatcher) {
        RoXmlHttpRequest().invokeKroki(pumlCode, agr)
    }

}
