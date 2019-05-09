package org.ro.handler

import org.ro.core.TransferObject
import org.ro.core.event.LogEntry

/**
 * Interface for handling asynchronous XHR responses.
 * Due to the fact that XMLHttpRequests are called asynchronously, responses may arrive in random order as well.
 * @see: https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern
 * COR simplifies implementation of Dispatcher.
 *
 * Implementing classes are responsible for:
 * @item creating Objects from JSON responses,
 * @item creating/finding Observers (eg. ListObserver, ObjectObserver), and
 * @item setting Objects and Observers into LogEntry.
 *
 * @see org.ro.handler.BaseHandler for more details
 */

interface IResponseHandler {

    fun handle(logEntry: LogEntry)
    fun canHandle(jsonStr: String): Boolean
    fun doHandle()
    fun parse(jsonStr: String): TransferObject?
}

