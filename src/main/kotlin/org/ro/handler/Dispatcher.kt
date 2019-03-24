package org.ro.handler

import org.ro.core.event.LogEntry

/**
 * Delegates responses to handlers, acts as Facade.
 * @see: https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern
 */
object Dispatcher {
    private var delegate: IResponseHandler;

    //TODO sequence of handlers should follow frequency of invocation in order minimize the time taken by unneeded calls to 'canHandle()'
    private var first = ResultHandler()
    private var second = ActionHandler()
    private var third = ServiceHandler()
    private var forth = ResultListHandler()
    private var fifth = TObjectHandler()
    private var sixth = LayoutHandler()
    private var seventh = PropertyHandler()
    private var eighth = PropertyDescriptionHandler()
    private var nineth = MemberHandler()
    private var tenth = HttpErrorHandler()
    

    private var last: DefaultHandler = DefaultHandler()

    init {
        first.successor = second
        second.successor = third
        third.successor = forth
        forth.successor = fifth
        fifth.successor = sixth
        sixth.successor = seventh
        seventh.successor = eighth
        eighth.successor = nineth
        nineth.successor = tenth
        tenth.successor = last

        delegate = first
    }

    fun handle(logEntry: LogEntry) {
        delegate.handle(logEntry)
    }
}