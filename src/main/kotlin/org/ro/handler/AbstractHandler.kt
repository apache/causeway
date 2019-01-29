package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.core.event.LogEntry
import org.ro.to.Extensions

/**
 *  Common 'abstract' superclass of Response Handlers.
 *  Constructor should not be called.
 */
open class AbstractHandler : IResponseHandler {
    var successor: IResponseHandler? = null
    protected var logEntry = LogEntry("")

    /**
     * @see https://en.wikipedia.org/wiki/Template_method_pattern
     * @param logEntry
     */
    override fun handle(logEntry: LogEntry) {
        this.logEntry = logEntry
        val jsonStr: String? = logEntry.getResponse()
        if (null === jsonStr) {
            console.log("jsonStr == null : " + logEntry.url)
        } else {
            if (canHandle(jsonStr)) {
                doHandle(jsonStr)
            } else {
                successor!!.handle(logEntry)
            }
        }
    }
    
    /**
     * Default implementation - should be overridden in subclasses.
     * @param jsonObj
     * @return
     */
    override fun canHandle(jsonStr: String): Boolean {
        return true
    }

    /**
     * Must be overridden in subclasses
     * @param jsonObj
     * @return
     */
    override fun doHandle(jsonStr: String) {
    }

    fun asExtensions(jsonObj: JsonObject): Extensions {
        val extensionJS = jsonObj["extensions"].jsonObject
        return Extensions(extensionJS)
    }

    fun hasMembers(jsonObj: JsonObject): Boolean {
        val members = jsonObj["members"].jsonArray
        return members.size > 0
    }

    fun isService(jsonObj: JsonObject): Boolean {
        val extensions = asExtensions(jsonObj)
        return extensions.isService
    }

}
