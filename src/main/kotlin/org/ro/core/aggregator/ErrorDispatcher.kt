package org.ro.core.aggregator

import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.to.HttpError
import org.ro.ui.ErrorAlert

class ErrorDispatcher : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        val error = logEntry.getTransferObject() as HttpError
        val url = logEntry.url
        val message = error.message
        EventStore.fault(url, message)
        ErrorAlert(logEntry).open()
    }

}
