package org.ro.core.event

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
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
    var start: Int = createdAt.getMilliseconds()

    @ContextualSerialization
    var updatedAt: Date? = null

    @ContextualSerialization
    private var lastAccessedAt: Date? = null

    private var fault: String? = null

    @ContextualSerialization
    var offset: Int = 0

    @ContextualSerialization
    var duration: Int = 0

    var cacheHits = 0             // FIXME always 0
    var observer: IObserver? = null
    var obj: TransferObject? = null

    // alternative constructor for UI events (eg. from user interaction)
    constructor(title: String) : this("", "", "") {
        this.title = title
        state = EventState.VIEW
    }

    private fun calculate() {
        // FIXME duration and offset are sometimes ouseide expected range

        duration = updatedAt!!.getMilliseconds() - start
        val logStartTime = EventStore.getLogStartTime()
        if (logStartTime != null) {
            offset = start - logStartTime
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

    override fun toString(): String {
        var s = "\n"
        s += "url:= $url\n"
        if (getObj() == null) {
            s += "obj:= null\n"
        } else {
            s += "obj:= ${getObj()!!::class.simpleName}\n"
        }
        if (observer == null) {
            s += "observer:= null\n"
        } else {
            s += "observer:= ${observer!!::class.simpleName}\n"
        }
        s += "response:= $response\n"
        return s
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
