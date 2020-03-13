package org.ro.core.event

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.ro.utils.Utils.removeHexCode
import org.ro.core.aggregator.BaseAggregator
import org.ro.to.TransferObject
import org.ro.ui.kv.UiManager
import pl.treksoft.kvision.html.ButtonStyle
import kotlin.js.Date

// use color codes from css instead?
enum class EventState(val id: String, val iconName: String, val style: ButtonStyle) {
    INITIAL("INITIAL", "fas fa-power-off", ButtonStyle.LIGHT),
    RUNNING("RUNNING", "fas fa-play-circle", ButtonStyle.WARNING),
    ERROR("ERROR", "fas fa-exclamation-circle", ButtonStyle.DANGER),
    SUCCESS("SUCCESS", "fas fa-check-circle", ButtonStyle.SUCCESS),
    VIEW("VIEW", "fas fa-info-circle", ButtonStyle.INFO),
    DUPLICATE("DUPLICATE", "fas fa-link", ButtonStyle.OUTLINESUCCESS),
    CLOSED("CLOSED", "fas fa-times-circle", ButtonStyle.OUTLINEINFO),
    RELOAD("RELOAD", "fas fa-retweet", ButtonStyle.OUTLINEWARNING),
    MISSING("MISSING","fas fa-bug", ButtonStyle.OUTLINEDANGER)
    // IMPROVE multiple aspects intermangled: req/resp, view, as well as cache
    // encapsulate access with managers?
}

@Serializable
data class LogEntry(
        val url: String,
        val method: String? = "",
        val request: String = "",
        val subType: String = "json",
        @ContextualSerialization val createdAt: Date = Date()) {
    var state = EventState.INITIAL
    var title: String = ""
    var requestLength: Int = 0 // must be accessible (public) for LogEntryTable
    var response = ""
    var responseLength: Int = 0 // must be accessible (public) for LogEntryTable

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
    private val aggregators by lazy { mutableListOf<BaseAggregator>() }

    @ContextualSerialization
    var obj: Any? = null

    // alternative constructor for UI events (eg. from user interaction)
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
        this.responseLength = response.length
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
            is TransferObject-> obj as TransferObject
            else -> null
        }
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
        val protocolHostPort = UiManager.getUrl()
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

    fun getAggregator(): BaseAggregator? {
        // is the last agg always the right one?
        return aggregators.last()
    }

    fun addAggregator(aggregator: BaseAggregator) {
        if (aggregators.size > 0) {
 /*           console.log("[LogEntry.addAggregator()]")
            console.log(aggregators)
            console.log(aggregator)*/
        }
        aggregators.add(aggregator)
    }

    fun matches(reSpec : ResourceSpecification) : Boolean{
       return url.equals(reSpec.url) && subType.equals(reSpec.subType)
    }

}
