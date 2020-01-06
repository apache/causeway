package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.Restful

class RestfulDispatcher() : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        val restful = logEntry.getTransferObject() as Restful
        restful.links.forEach {
            when {
                it.rel.endsWith("/menuBars") -> {
                    RoXmlHttpRequest().invoke(it, NavigationDispatcher())
                }
                it.rel.equals("self") -> {
                }
                it.rel.endsWith("/services") -> {
                }
                else -> invoke(it)
            }
        }
    }

}
