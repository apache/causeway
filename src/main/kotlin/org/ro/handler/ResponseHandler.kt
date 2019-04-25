package org.ro.handler

import org.ro.core.Session.url
import org.ro.core.event.LogEntry

/**
 * Delegates responses to handlers, acts as Facade.
 * @see: https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern
 */
object ResponseHandler {
    private var delegate: IResponseHandler;

    //TODO sequence of handlers should follow frequency of invocation in order minimize the time taken by unneeded calls to 'canHandle()'
    private var _1 = ResultHandler()
    private var _2 = ActionHandler()
    private var _3 = ServiceHandler()
    private var _4 = ResultListHandler()
    private var _5 = TObjectHandler()
    private var _6 = LayoutHandler()
    private var _7 = PropertyHandler()
    private var _8 = MemberHandler()
    private var _10 = HttpErrorHandler()
    private var last = DefaultHandler()

    init {
        _1.successor = _2
        _2.successor = _3
        _3.successor = _4
        _4.successor = _5
        _5.successor = _6
        _6.successor = _7
        _7.successor = _8
        _8.successor = _10
        _10.successor = last

        delegate = _1
    }

    fun handle(logEntry: LogEntry) {
        delegate.handle(logEntry)
    }
}