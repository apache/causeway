package org.ro.handler


import org.ro.core.TransferObject
import org.ro.core.Utils
import org.ro.core.event.LogEntry

/**
 *  Common 'abstract' superclass of Response Handlers.
 *  Constructor should not be called.
 */

abstract class BaseHandler : IResponseHandler {
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
            Utils.debug("jsonStr == null : " + logEntry.url)
        } else {
            if (canHandle(jsonStr)) {
                Utils.debug(this::class)
                doHandle()
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
        var answer = false
        try {
            val obj = parse(jsonStr)
            logEntry.setObj(obj)
            answer = true
        } catch (ex: Exception) {
        }
        return answer;
    }

    /**
     * May be overridden in subclasses
     * @return
     */
    override fun doHandle() {
        update()
    }

    /**
     * Must be overridden in subclasses
     * @return
     */
    override fun parse(jsonStr: String): TransferObject? {
        throw Exception("Subclass Responsibility")
    }
    
    protected fun update() {
        logEntry.observer!!.update(logEntry)
    }

}
