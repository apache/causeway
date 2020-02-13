package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.utils.DomUtils

class DiagramDispatcher(private val uuid: String) : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        val response = logEntry.response
        DomUtils.appendTo(response, uuid)
    }

}
