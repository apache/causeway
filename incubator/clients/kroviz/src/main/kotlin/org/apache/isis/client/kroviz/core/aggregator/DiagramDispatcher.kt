package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.utils.DomHelper

class DiagramDispatcher(private val uuid: String) : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val response = logEntry.response
        DomHelper.appendTo(response, uuid)
    }

}
