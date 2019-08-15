package org.ro.core.event

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.ro.core.aggregator.Aggregator
import org.ro.to.TransferObject
import kotlin.js.Date

enum class EventState(val id: String, val iconName: String) {
    INITIAL("INITIAL", "fa-power-off"),
    RUNNING("RUNNING", "fa-play-circle"),
    ERROR("ERROR", "fa-exclamation-circle"),
    SUCCESS("SUCCESS", "fa-check-circle"),
    VIEW("VIEW", "fa-info-circle"),
    CLOSED("CLOSED", "fa-times-circle")
}

@Serializable
data class LogEntry(
        val url: String,
        val method: String? = "",
        val request: String = "") {
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
    var createdAt = Date()

    @ContextualSerialization
    var start: Long = createdAt.getMilliseconds().toLong()

    @ContextualSerialization
    var updatedAt: Date? = null

    @ContextualSerialization
    private var lastAccessedAt: Date? = null

    private var fault: String? = null

    @ContextualSerialization
    var offset: Long = 0L

    @ContextualSerialization
    var duration: Long = 0L

    var cacheHits = 0
    var aggregator: Aggregator? = null
    var obj: TransferObject? = null

    // alternative constructor for UI events (eg. from user interaction)
    constructor(title: String) : this("", "", "") {
        this.title = title
        state = EventState.VIEW
    }

    private fun calculate() {
        duration = updatedAt!!.getMilliseconds() - start
        offset = start - EventStore.logStartTime

        if (duration < 0 || offset < 0) {
            console.log("[LogEntry.calculate] duration/offset out of range")
            console.log(this)
        }
    }

    fun setError(error: String) {
        updatedAt = Date()
        calculate()
        fault = error
        state = EventState.ERROR
    }

    fun setClose() {
        updatedAt = Date()
        state = EventState.CLOSED
    }

    fun setSuccess() {
        updatedAt = Date()
        calculate()
        this.responseLength = response.length
        state = EventState.SUCCESS
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
        //TODO use value from Session
        result = result.replace("http://localhost:8080/restful/", "")
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
