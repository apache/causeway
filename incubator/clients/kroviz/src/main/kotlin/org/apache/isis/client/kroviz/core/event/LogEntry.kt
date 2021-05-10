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

import io.kvision.html.ButtonStyle
import io.kvision.panel.SimplePanel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.Utils.removeHexCode
import kotlin.js.Date

// use color codes from css instead?
enum class EventState(val id: String, val iconName: String, val style: ButtonStyle) {
    INITIAL("INITIAL", "fas fa-power-off", ButtonStyle.LIGHT),
    RUNNING("RUNNING", "fas fa-play-circle", ButtonStyle.WARNING),
    ERROR("ERROR", "fas fa-exclamation-circle", ButtonStyle.DANGER),
    SUCCESS("SUCCESS", "fas fa-check-circle", ButtonStyle.SUCCESS),
    VIEW("VIEW", "fas fa-eye", ButtonStyle.INFO),
    DUPLICATE("DUPLICATE", "fas fa-link", ButtonStyle.OUTLINESUCCESS),
    CLOSED("CLOSED", "fas fa-eye-slash", ButtonStyle.OUTLINEINFO),
    RELOAD("RELOAD", "fas fa-retweet", ButtonStyle.OUTLINEWARNING),
    MISSING("MISSING", "fas fa-bug", ButtonStyle.OUTLINEDANGER)
    // IMPROVE multiple aspects intermangled: req/resp, view, as well as cache
    // encapsulate access with managers?
}

@OptIn(ExperimentalJsExport::class)
@Serializable
@JsExport
data class LogEntry(
        val url: String,
        val method: String? = "",
        val request: String = "",
        val subType: String = Constants.subTypeJson,
        @Contextual val createdAt: Date = Date()) {
    var state = EventState.INITIAL
    var title: String = ""
    var requestLength: Int = 0 // must be accessible (public) for LogEntryTable
    var response = ""
    var responseLength: Int = 0 // must be accessible (public) for LogEntryTable

    init {
        state = EventState.RUNNING
        title = url // stripHostPort(url)
        requestLength = request?.length
                ?: 0 // if this is simplyfied to request.length, Tabulator.js goes in ERROR and EventLogTable shows no entries
    }

    @Contextual
    var updatedAt: Date? = null

    @Contextual
    private var lastAccessedAt: Date? = null

    private var fault: String? = null

    @Contextual
    var duration: Int = 0

    var cacheHits = 0

    val aggregators = mutableListOf<@Contextual BaseAggregator>()
    var nOfAggregators: Int = 0 // must be accessible (public) for LogEntryTable

    @Contextual
    var obj: Any? = null

    @Contextual
    var panel: SimplePanel? = null

    // alternative constructor for UI events (eg. from user interaction)
    @JsName("secondaryConstructor")
    constructor(title: String, aggregator: BaseAggregator) : this("", "", "") {
        this.title = title
        this.addAggregator(aggregator)
        state = EventState.VIEW
    }

    private fun calculate() {
        val date = Date()
        updatedAt = date
        duration = (date.getTime() - createdAt.getTime()).toInt()
    }

    fun setError(error: String) {
        calculate()
        fault = error
        state = EventState.ERROR
    }

    fun setUndefined(error: String) {
        calculate()
        fault = error
        state = EventState.MISSING
    }

    fun setClose() {
        updatedAt = Date()
        state = EventState.CLOSED
    }

    fun setSuccess() {
        calculate()
        responseLength = response.length
        state = EventState.SUCCESS
    }

    fun setCached() {
        state = EventState.DUPLICATE
    }

    fun setReload() {
        state = EventState.RELOAD
    }

    fun getTransferObject(): TransferObject? {
        return when (obj) {
            is TransferObject -> obj as TransferObject
            else -> null
        }
    }

    fun setTransferObject(to: TransferObject) {
        this.obj = to
    }

    // region response
    /**
     * This is for access from the views only.
     * DomainObjects have to use retrieveResponse,
     * since we want to have access statistics
     * and a cache function.
     * @return
     */
    fun getResponse(): String {
        return response
    }

    fun hasResponse(): Boolean {
        return response != ""
    }

    fun retrieveResponse(): String {
        lastAccessedAt = Date()
        cacheHits++
        return response
    }

    //end region response

    private fun stripHostPort(url: String): String {
        var result = url
        val signature = "restful/"
        if (url.contains(signature)) {
            val protocolHostPort = UiManager.getUrl()
            result = result.replace(protocolHostPort + signature, "")
            result = removeHexCode(result)
        }
        return result
    }

    fun isView(): Boolean {
        return isOpenView() || isClosedView()
    }

    private fun isOpenView(): Boolean {
        return state == EventState.VIEW
    }

    fun isClosedView(): Boolean {
        return state == EventState.CLOSED
    }

    fun isError(): Boolean {
        return fault != null
    }

    fun getAggregator(): BaseAggregator? {
        //TODO is the last agg always the right one?
        return aggregators.last()
    }

    fun addAggregator(aggregator: BaseAggregator) {
        aggregators.add(aggregator)
        nOfAggregators = aggregators.size
    }

    fun matches(reSpec: ResourceSpecification): Boolean {
        return url == reSpec.url && subType.equals(reSpec.subType)
    }

}
