package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.Link
import org.ro.to.Restful

class RestfulDispatcher() : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        val restful = logEntry.getTransferObject() as Restful
        restful.links.forEach {
            when {
                it.rel.endsWith("/menuBars") -> invokeNavigation(it)
                it.rel == "self" -> noop()
                it.rel.endsWith("/services") -> noop()
                else -> invokeSystem(it)
            }
        }
    }

    private fun invokeNavigation(it: Link) {
        RoXmlHttpRequest().invoke(it, NavigationDispatcher())
    }

    private fun invokeSystem(it: Link) {
        RoXmlHttpRequest().invoke(it, SystemAggregator())
    }

    private fun noop() {
        //do nothing
    }

}
