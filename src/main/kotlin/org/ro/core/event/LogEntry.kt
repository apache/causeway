package org.ro.core.event

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.ro.core.Session
import org.ro.core.aggregator.Aggregator
import org.ro.to.TransferObject
import pl.treksoft.kvision.core.Col
import kotlin.js.Date

//Eventually color codes from css instead
enum class EventState(val id: String, val iconName: String, val color: Col ) {
    INITIAL("INITIAL", "fa-power-off", Col.GRAY),
    RUNNING("RUNNING", "fa-play-circle", Col.YELLOW),
    ERROR("ERROR", "fa-exclamation-circle", Col.RED),
    SUCCESS("SUCCESS", "fa-check-circle", Col.GREEN),
    VIEW("VIEW", "fa-info-circle", Col.BLUE),
    CACHE_USED ("CACHE_USED", "fa-caret-circle-left", Col.VIOLET),
    CLOSED("CLOSED", "fa-times-circle", Col.LIGHTBLUE)
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
    var aggregator: Aggregator? = null
    var obj: TransferObject? = null

    // alternative constructor for UI events (eg. from user interaction)
    constructor(title: String) : this("", "", "") {
        this.title = title
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

    fun getObj(): TransferObject? {
        return obj
    }

    fun setObj(obj: TransferObject?) {
        this.obj = obj
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

    private fun removeHexCode(input: String): String {
        var output = ""
        val list: List<String> = input.split("/")
        //split string by "/" and remove parts longer than 40chars
        for (s in list) {
            output += "/"
            output += if (s.length < 40) {
                s
            } else {
                "..."
            }
        }
        return output
    }

    fun isView(): Boolean {
        return !isUrl()
    }

    fun isUrl(): Boolean {
        return url.startsWith("http")
    }

    fun isClosedView(): Boolean {
        return state == EventState.CLOSED
    }

    fun isError(): Boolean {
        return fault != null
    }

}
