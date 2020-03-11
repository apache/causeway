package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.utils.DomHelper
import org.w3c.dom.MimeType

class DiagramDispatcher(private val uuid: String) : BaseAggregator() {

    override fun update(logEntry: LogEntry, mimeType: String) {
        val response = logEntry.response
        DomHelper.appendTo(response, uuid)
    }

}
