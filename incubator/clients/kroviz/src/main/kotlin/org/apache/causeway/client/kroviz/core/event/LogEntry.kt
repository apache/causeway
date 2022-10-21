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
package org.apache.causeway.client.kroviz.core.event

import io.kvision.html.ButtonStyle
import io.kvision.panel.SimplePanel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.apache.causeway.client.kroviz.core.aggregator.ActionDispatcher
import org.apache.causeway.client.kroviz.core.aggregator.BaseAggregator
import org.apache.causeway.client.kroviz.to.*
import org.apache.causeway.client.kroviz.to.bs3.Grid
import org.apache.causeway.client.kroviz.to.mb.Menubars
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.ui.core.ViewManager
import org.w3c.files.Blob
import kotlin.js.Date

// use color codes from css instead?
enum class EventState(val id: String, val iconName: String, val style: ButtonStyle) {
    INITIAL("INITIAL", "fas fa-power-off", ButtonStyle.LIGHT),
    RUNNING("RUNNING", "fas fa-hourglass-start", ButtonStyle.WARNING),
    ERROR("ERROR", "fas fa-exclamation-circle", ButtonStyle.DANGER),
    SUCCESS_JS("SUCCESS_JS", "fab fa-js", ButtonStyle.SUCCESS),
    SUCCESS_XML("SUCCESS_XML", "fas fa-code", ButtonStyle.SUCCESS),
    SUCCESS_IMG("SUCCESS_IMG", "fas fa-image", ButtonStyle.SUCCESS),
    VIEW("VIEW", "fas fa-eye", ButtonStyle.INFO),
    DIALOG("DIALOG", "fas fa-o-commenting", ButtonStyle.LIGHT),
    USER_ACTION("ACTION", "fas fa-user-times", ButtonStyle.INFO),
    DUPLICATE("DUPLICATE", "fas fa-copy", ButtonStyle.OUTLINESUCCESS),
    CLOSED("CLOSED", "fas fa-eye-slash", ButtonStyle.OUTLINEINFO),
    RELOAD("RELOAD", "fas fa-retweet", ButtonStyle.OUTLINEWARNING),
    MISSING("MISSING", "fas fa-bug", ButtonStyle.OUTLINEDANGER),
    // IMPROVE multiple aspects intermangled: req/resp, view, as well as cache
    // encapsulate access with managers?
}

@Serializable
data class LogEntry(
    @Contextual val rs: ResourceSpecification,
    val method: String? = "",
    val request: String = "",
    @Contextual val createdAt: Date = Date(),
) {
    val url: String = rs?.url

    //?. is required, otherwise Tabulator.js/EventLogTable shows no entries
    val subType = rs?.subType

    //?. is required, otherwise Tabulator.js/EventLogTable shows no entries
    var state = EventState.INITIAL
    var title: String = ""
    var requestLength: Int = 0 // must be accessible (public) for LogEntryTable
    var response = ""

    @Contextual
    var blob: Blob? = null
    var responseLength: Int = 0 // must be accessible (public) for LogEntryTable
    var type: String = ""

    init {
        state = EventState.RUNNING
        title = url
        requestLength = request?.length
            ?: 0 // ?. is required, otherwise Tabulator.js/EventLogTable shows no entries
    }

    @Contextual
    var updatedAt: Date? = null

    @Contextual
    private var lastAccessedAt: Date? = null

    private var fault: String? = null

    @Contextual
    var duration: Int = 0

    var cacheHits = 0

    @Contextual
    val aggregators = mutableListOf<@Contextual BaseAggregator>()
    var nOfAggregators: Int = 0 // must be accessible (public) for LogEntryTable

    @Contextual
    var obj: Any? = null

    @Contextual
    var panel: SimplePanel? = null

    var runningAtStart = 0
    var runningAtEnd = 0

    // alternative constructor for UI events (eg. from user interaction)
    @JsName("secondaryConstructor")
    constructor(title: String, aggregator: BaseAggregator) : this(ResourceSpecification(""), "", "") {
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
        type = Represention.ERROR.type
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
        if (responseLength == 0) {
            // it's a blob
            val size = blob?.size ?: 0
            responseLength = size.toInt()
        }
        state = when {
            url.startsWith(Constants.krokiUrl) -> EventState.SUCCESS_IMG
            subType == Constants.subTypeXml -> EventState.SUCCESS_XML
            else -> EventState.SUCCESS_JS
        }
    }

    fun setCached() {
        state = EventState.DUPLICATE
    }

    internal fun isCached(rs: ResourceSpecification, method: String): Boolean {
        return when {
            hasResponse()
                    && this.method == method
                    && subType == rs.subType -> true
            isView() -> true
            else -> false
        }
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
        console.log("[LE.setTransferObject]")
        this.obj = to
        when (to) {
            is WithLinks -> {
                this.type = extractType(to)
            }
            is Grid -> {
                this.type = Relation.LAYOUT.type
            }
            is Icon -> {
                this.type = Relation.OBJECT_ICON.type
            }
            is Blob -> {
                this.type = Represention.IMAGE_PNG.type
            }
            is Menubars -> {
                this.type = Represention.LAYOUT_MENUBARS.type
            }
            is HttpError -> {
                this.type = Represention.ERROR.type
            }
            is TObject -> {
                when {
                    to == null -> {
                        this.state = EventState.MISSING
                        this.type = Represention.ERROR.type
                        console.log("to == null for response:")
                        console.log(response)
                    }
                }
            }
            else -> {
                console.log(to)
            }
        }
    }

    //TODO this should be moved to a ValueSemanticsProvider
    private fun extractType(wl: WithLinks): String {
        val firstLink = wl.getLinks().firstOrNull()!!
        val result = firstLink.simpleType()
        if (result.trim().length == 0) {
            console.log("[LE.extractType]")
            console.log(obj)
            console.log(result)
        }
        return result
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

    private fun hasResponse(): Boolean {
        return response != ""
    }

    fun retrieveResponse(): String {
        lastAccessedAt = Date()
        incrementCacheHits()
        return response
    }

//end region response

    fun incrementCacheHits() {
        cacheHits++
    }

    fun isSuccess(): Boolean {
        return state.name.startsWith("SUCCESS")
    }

    fun isRunning(): Boolean {
        return state == EventState.RUNNING
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
        //TODO the last aggt is not always the right one
        // callers need to filter  !!!
        if (aggregators.size == 0) {
            console.log("[LE.getAggregator] no Aggregator(s) yet")
            console.log(this)
            return null
        } else {
            return aggregators.last()
        }
    }

    fun addAggregator(aggregator: BaseAggregator) {
        if (aggregator is ActionDispatcher) {
            ViewManager.setBusyCursor()
        }
        aggregators.add(aggregator)
        nOfAggregators = aggregators.size
    }

    fun matches(reSpec: ResourceSpecification): Boolean {
        return url == reSpec.url && subType.equals(reSpec.subType)
    }

}
