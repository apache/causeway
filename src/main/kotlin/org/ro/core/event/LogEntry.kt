package org.ro.core.event

import kotlinext.js.Object
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.ro.core.Session
import org.ro.core.Utils.removeHexCode
import org.ro.core.aggregator.IAggregator
import org.ro.to.TransferObject
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.panel.VPanel
import kotlin.js.Date

//Eventually color codes from css instead
enum class EventState(val id: String, val iconName: String, val color: Col) {
    INITIAL("INITIAL", "fas fa-power-off", Col.GRAY),
    RUNNING("RUNNING", "fas fa-play-circle", Col.YELLOW),
    ERROR("ERROR", "fas fa-exclamation-circle", Col.RED),
    SUCCESS("SUCCESS", "fas fa-check-circle", Col.GREEN),
    VIEW("VIEW", "fas fa-info-circle", Col.BLUE),
    CACHE_USED("CACHE_USED", "fas fa-caret-circle-left", Col.VIOLET),
    CLOSED("CLOSED", "fas fa-times-circle", Col.LIGHTBLUE)
}

@Serializable
data class LogEntry(
        val url: String,
        val method: String? = "",
        val request: String = "",
        @ContextualSerialization val createdAt: Date = Date()) {
    var state = EventState.INITIAL
    var title: String = ""
    var requestLength: Int = 0
    var response = ""
    var responseLength: Int = 0
    var isRoot: Boolean = false

    init {
        state = EventState.RUNNING
        title = stripHostPort(url)
        requestLength = request.length
    }

    @ContextualSerialization
    var updatedAt: Date? = null

    @ContextualSerialization
    private var lastAccessedAt: Date? = null

    private var fault: String? = null

    @ContextualSerialization
    var duration: Int = 0

    var cacheHits = 0
    var aggregator: IAggregator? = null

    @ContextualSerialization
    var obj: Any? = null

    // alternative constructor for UI events (eg. from user interaction)
    constructor(title: String, aggregator: IAggregator) : this("", "", "") {
        this.title = title
        this.aggregator = aggregator
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

    fun setClose() {
        updatedAt = Date()
        state = EventState.CLOSED
    }

    fun setSuccess() {
        calculate()
        this.responseLength = response.length
        state = EventState.SUCCESS
    }

    fun setCached() {
        state = EventState.CACHE_USED
    }

    fun getTransferObject(): TransferObject? {
        return obj as TransferObject
    }

    fun setTransferObject(to: TransferObject?) {
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
        val protocolHostPort = Session.url
        result = result.replace(protocolHostPort + "restful/", "")
        result = removeHexCode(result)
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

}
