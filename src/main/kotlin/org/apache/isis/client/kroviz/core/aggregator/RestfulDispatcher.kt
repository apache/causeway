package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.RoXmlHttpRequest
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Restful

class RestfulDispatcher() : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
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

}
