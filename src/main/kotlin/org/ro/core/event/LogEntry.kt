package org.ro.core.event

import org.ro.view.ImageRepository
import pl.treksoft.kvision.types.Date

class LogEntry(var url: String, var method: String? = null, private var request: String? = null) {
    private var icon: Any? = null

    init {
        icon = ImageRepository.YellowIcon
    }

    private var urlTitle: String? = null

    init {
        urlTitle = stripHostPort(url)
    }

    var createdAt = Date()
    var start: Int = createdAt.getMilliseconds()
    var updatedAt: Date? = null
    private var lastAccessedAt: Date? = null
    private var offset: Int? = null
    private var fault: String? = null
    private var requestLength: Int? = null

    init {
        requestLength = request!!.length
    }

    private var responseLength: Int? = null
    var response: String = ""
    private var duration: Int = 0
    var obj: Any? = null
    var cacheHits: Int = 0
    var observer: ILogEventObserver? = null

    // alternative constructor for UI events (eg. from user interaction)
    constructor(description: String) : this(description, null, null) {
        icon = ImageRepository.BlueIcon
    }

    private fun calculate() {
        duration = updatedAt!!.getMilliseconds() - start
        val logStartTime: Int? = EventLog.getLogStartTime()
        if (logStartTime != null) {
            offset = start - logStartTime
        }
    }

    fun setError(error: String) {
        updatedAt = Date()
        calculate()
        fault = error
        icon = ImageRepository.RedIcon
    }

    fun setClose() {
        updatedAt = Date()
        icon = ImageRepository.TimesIcon
    }

    fun setSuccess(response: String) {
        updatedAt = Date()
        calculate()
        this.response = response //.replace("\r\n", "")
        responseLength = response.length
        icon = ImageRepository.GreenIcon
        observer!!.update(this)
    }

    fun initListObserver(): ListObserver {
        observer = ListObserver()
        return observer as ListObserver
    }

    override fun toString(): String {
        var s = "$url/n"
        s += "$method/n"
        return s
    }

    // region response
    /**
     * This is for access from the views only.
     * DomainObjects have to use retrieveResponse,
     * since we want to have access statistics
     * and a cache fun.
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

    private fun stripHostPort(url: String): String? {
        var result = url
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

    fun printString(): String {
        var result = "[url: $url \\n"
        result += "arguments: $request \\n"
        result += "response: $response ]"
        return result
    }

    fun isView(): Boolean {
        return ImageRepository.BlueIcon == icon
    }

    fun isClosedView(): Boolean {
        return ImageRepository.TimesIcon == icon
    }

}