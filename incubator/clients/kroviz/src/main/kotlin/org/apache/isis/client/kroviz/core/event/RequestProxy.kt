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
import org.apache.isis.client.kroviz.core.aggregator.ObjectAggregator
import org.apache.isis.client.kroviz.core.aggregator.SvgDispatcher
import org.apache.isis.client.kroviz.handler.ResponseHandler
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.TObject
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
            isCached(rs, link.method) -> processCached(rs, aggregator)
            else -> RoXmlHttpRequest().process(link, aggregator, subType)
        }
    }

    private fun isNotRenderedYet(aggregator: BaseAggregator?): Boolean {
        if (aggregator != null && aggregator is AggregatorWithLayout) {
            return !aggregator.dpm.isRendered
        } else {
            return false
        }
    }

    private fun processCached(rs: ResourceSpecification, aggregator: BaseAggregator?) {
        console.log("[RP.processCached]")
        val le = EventStore.findBy(rs)!!
        le.retrieveResponse()
        if (aggregator == null) {
            ResponseHandler.handle(le)
        } else {
            console.log(aggregator)
            console.log(le)
            aggregator.update(le, le.subType)
        }
        cached(rs)
    }

    private fun cached(rs: ResourceSpecification): LogEntry {
        val entry: LogEntry = EventStore.findBy(rs)!!
        entry.setCached()
        EventStore.updateStatus(entry)
        return entry
    }

    fun invokeNonREST(link: Link, aggregator: BaseAggregator?, subType: String = Constants.subTypeXml) {
        val rs = ResourceSpecification(link.href)
        when {
            isCached(rs, link.method) -> processCached(rs, aggregator)
            else -> RoXmlHttpRequest().processNonREST(link, aggregator, subType)
        }
    }

    private fun isCached(rs: ResourceSpecification, method: String): Boolean {
        val le = EventStore.findBy(rs)
        if (le == null) {
            return false
        } else {
            val result = le.isCached(rs, method)
            if (result) le.setCached()
            return result
        }
    }

    fun invokeKroki(pumlCode: String, agr: SvgDispatcher) {
        RoXmlHttpRequest().invokeKroki(pumlCode, agr)
    }

    // there may be more than one aggt - which may break this code
    // we are coming from a parented collection ...
    // we can assume the object hat been loaded as part of the collection before
    fun load(tObject: TObject) {
        console.log("[RP.load]")
        val aggregator = ObjectAggregator(tObject.title)
        // ASSUMPTION: there can be max one LogEntry for an Object
        val le = EventStore.findBy(tObject)
        console.log(le)
        if (le != null) {
            le.addAggregator(aggregator)
            console.log(aggregator)
            console.log(le)
            aggregator.update(le, le.subType)
        }
    }

}
