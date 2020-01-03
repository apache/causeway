package org.ro.handler

import org.ro.core.event.LogEntry
import org.ro.to.TransferObject

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
        val response: String? = logEntry.getResponse()
        if (null !== response) {
            if (canHandle(response)) {
                doHandle()
            } else {
                successor!!.handle(logEntry)
            }
        }
    }

    /**
     * Default implementation - may be overridden in subclasses.
     * @param jsonObj
     * @return
     */
    override fun canHandle(response: String): Boolean {
        try {
            val obj = parse(response)
            logEntry.setTransferObject(obj)
            return true
        } catch (ex: Exception) {
            return false
        }
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
    override fun parse(response: String): TransferObject? {
        throw Exception("Subclass Responsibility")
    }

    protected fun update() {
        //TODO is the first agg the right one?
        logEntry.getAggregator()!!.update(logEntry)
    }

}
