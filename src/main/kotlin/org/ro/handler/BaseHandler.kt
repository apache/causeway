package org.ro.handler

import org.ro.core.event.LogEntry
import org.ro.to.TransferObject

/**
 * Superclass of Response Handlers which have to handle asynchronous XHR responses.
 * Due to the fact that XMLHttpRequests are called asynchronously, responses may arrive in random order.
 * @see: https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern
 * COR simplifies implementation of Dispatcher.
 *
 * Implementing classes are responsible for:
 * @item creating Objects from JSON responses,
 * @item creating/finding Aggregators (eg. ListAggregator, ObjectAggregator), and
 * @item setting Objects and Aggregators into LogEntry.
 */
abstract class BaseHandler {
    var successor: BaseHandler? = null
    protected var logEntry = LogEntry("")

    /**
     * @see https://en.wikipedia.org/wiki/Template_method_pattern
     * @param logEntry
     */
    open fun handle(logEntry: LogEntry) {
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
    open fun canHandle(response: String): Boolean {
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
    open fun doHandle() {
        update()
    }

    /**
     * Must be overridden in subclasses
     * @return
     */
    open fun parse(response: String): TransferObject? {
        throw Exception("Subclass Responsibility")
    }

    protected fun update() {
        logEntry.getAggregator()!!.update(logEntry)
    }

}
