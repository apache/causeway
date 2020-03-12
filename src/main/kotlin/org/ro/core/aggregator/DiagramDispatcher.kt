package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.utils.DomHelper

class DiagramDispatcher(private val uuid: String) : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val response = logEntry.response
        DomHelper.appendTo(response, uuid)
    }

}
