package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.core.Utils
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
        val jsonObj: JsonObject? = getJsonObject(logEntry)
        if (null == jsonObj) {
            console.log("jsonObj == null : " + logEntry.url)
        } else {
            if (canHandle(jsonObj)) {
                doHandle(jsonObj)
            } else {
                successor!!.handle(logEntry)
            }
        }
    }

    private fun getJsonObject(logEntry: LogEntry): JsonObject? {
        val jsonStr: String = logEntry.getResponse()
        return Utils().toJsonObject(jsonStr)
    }

    /**
     * Default implementation - should be overridden in subclasses.
     * @param jsonObj
     * @return
     */
    override fun canHandle(jsonObj: JsonObject): Boolean {
        return true
    }

    /**
     * Must be overridden in subclasses
     * @param jsonObj
     * @return
     */
    override fun doHandle(jsonObj: JsonObject) {
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
