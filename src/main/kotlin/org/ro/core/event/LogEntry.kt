package org.ro.core.event

import kotlinx.serialization.Serializable
import org.ro.core.TransferObject
import org.ro.view.table.ActionMenu
import kotlin.js.Date

enum class EventState(val id: String, val iconName: String) {
    INITIAL("INITIAL", "fa-power-off"),
    RUNNING("RUNNING", "fa-play-circle"),
    ERROR("ERROR", "fa-times-circle"),
    SUCCESS("SUCCESS", "fa-check-circle"),
    VIEW("VIEW", "fa-info-circle"),
    CLOSED("CLOSED", "fa-times-circle") //TODO should be different from ERROR?
}

@Serializable
data class LogEntry(
        val url: String,
        val method: String? = null,
        val request: String = "") {
    
    var state = EventState.INITIAL
    var menu: ActionMenu? = null
    var iconName = "img/2-2-clapperboard-png-thumb.png"

    init {
        state = EventState.RUNNING
        menu = ActionMenu("fa-ellipsis-h")
    }

    var title: String = ""

    init {
        title = stripHostPort(url)
    }

    var createdAt = Date()
    var start: Int = createdAt.getMilliseconds()
    var updatedAt: Date? = null
    private var lastAccessedAt: Date? = null
    var offset = 0
    private var fault: String? = null
    var requestLength = 0

    init {
        requestLength = request.length
    }

    var responseLength: Int? = null
    var response = ""
    var duration = 0
//    var obj: Any? = null
    var cacheHits = 0
    var observer: IObserver? = null
    var obj: TransferObject? = null

    // alternative constructor for UI events (eg. from user interaction)
    constructor(title: String) : this("", null, "") {
        this.title = title
        state = EventState.VIEW
    }

    private fun calculate() {
        duration = updatedAt!!.getMilliseconds() - start
        val logStartTime: Int? = EventStore.getLogStartTime()
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

    fun setSuccess(response: String) {
        updatedAt = Date()
        calculate()
        this.response = response //.replace("\r\n", "")
        responseLength = response.length
        state = EventState.SUCCESS
    }

    override fun toString(): String {
        var s = "url: $url\n"
        if (getObj() == null) {
            s += "obj: null\n"
        } else {
            s += "obj: ${getObj()!!::class.simpleName}\n"
        }
        if (observer == null) {
            s += "obsever: null\n"
        } else {
            s += "obsever: ${observer!!::class.simpleName}\n"
        }
        s += "response: $response\n"
        return s
    }

    fun getObj() : TransferObject? {
        return obj
    }
    fun setObj(obj: TransferObject?)  {
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

    fun match(search: String?): Boolean {
        // TODO  dismantle train.wreck
        return search?.let {
            url.contains(it, true) ?: false ||
                    response.contains(it, true) ?: false ||
                    method?.contains(it, true) ?: false
        } ?: true
    }

}