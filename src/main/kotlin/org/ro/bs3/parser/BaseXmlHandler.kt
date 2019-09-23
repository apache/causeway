package org.ro.bs3.parser

import org.ro.core.event.LogEntry
import org.ro.bs3.to.Bs3Object

abstract class BaseXmlHandler : IXmlHandler {
    var successor: IXmlHandler? = null
    protected var logEntry = LogEntry("")

    /**
     * @see https://en.wikipedia.org/wiki/Template_method_pattern
     * @param logEntry
     */
    override fun handle(xmlStr: String) {
    }

    /**
     * Default implementation - should be overridden in subclasses.
     * @param jsonObj
     * @return
     */
    override fun canHandle(xmlStr: String): Boolean {
        var answer = false
        try {

        } catch (ex: Exception) {
        }
        return answer
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
    override fun parse(xmlStr: String): Bs3Object? {
        throw Exception("Subclass Responsibility")
    }

    protected fun update() {
        // logEntry.aggregator!!.update(logEntry)
    }

}
