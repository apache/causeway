package org.apache.isis.client.kroviz.handler

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.ui.kv.Constants

/**
 * Handle responses to XmlHttpRequests asynchronously,
 * since they may arrive in arbitrary order.
 * @see: https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern
 * COR simplifies implementation of Dispatcher.
 *
 * Implementing classes are responsible for:
 * @item creating Objects by parsing responses (JSON/XML),
 * @item creating/finding Aggregators (eg. ListAggregator, ObjectAggregator), and
 * @item setting Objects and Aggregators into LogEntry.
 */
abstract class BaseHandler {
    var successor:BaseHandler? = null
    protected var logEntry =LogEntry("")

    /**
     * @see https://en.wikipedia.org/wiki/Template_method_pattern
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
     */
    open fun doHandle() {
        update()
    }

    /**
     * Must be overridden in subclasses
     */
    open fun parse(response: String):TransferObject? {
        throw Exception("Subclass Responsibility")
    }

    protected fun update() {
        logEntry.getAggregator()!!.update(logEntry, Constants.subTypeJson)
    }

}
