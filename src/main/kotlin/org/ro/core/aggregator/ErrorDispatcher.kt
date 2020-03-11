package org.ro.core.aggregator

import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.core.event.ResourceSpecification
import org.ro.to.HttpError
import org.ro.ui.ErrorAlert
import org.w3c.dom.MimeType

class ErrorDispatcher : BaseAggregator() {

    override fun update(logEntry: LogEntry, mimeType: String) {
        val error = logEntry.getTransferObject() as HttpError
        val url = logEntry.url
        val message = error.message
        val reSpec = ResourceSpecification(url)
        EventStore.fault(reSpec, message)
        ErrorAlert(logEntry).open()
    }

}
