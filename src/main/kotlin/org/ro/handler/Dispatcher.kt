package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.core.event.LogEntry

/**
 * Delegates responses to handlers, acts as Facade.
 * @see: https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern
 */
@ImplicitReflectionSerializer
object Dispatcher {
    private var delegate: IResponseHandler? = null

    //TODO sequence of handlers should follow frequency of invocation in order minimize the time taken by unneeded calls to 'canHandle()'
    private var first = ServiceHandler()
    private var second = ActionHandler()
    private var third = MemberHandler()
    private var forth = ListHandler()
    private var fifth = TObjectHandler()
    private var sixth = LayoutHandler()
    private var seventh = PropertyHandler()
    private var eighth = PropertyDescriptionHandler()

    private var last: DefaultHandler = DefaultHandler()

    init {
        first.successor = second
        second.successor = third
        third.successor = forth
        forth.successor = fifth
        fifth.successor = sixth
        sixth.successor = seventh
        seventh.successor = eighth
        eighth.successor = last

        delegate = first
    }

    fun handle(logEntry: LogEntry): Unit {
//        console.log(logEntry)
        delegate!!.handle(logEntry)
    }
}