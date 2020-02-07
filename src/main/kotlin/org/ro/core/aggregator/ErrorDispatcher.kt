package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.to.HttpError
import org.ro.ui.ErrorAlert

class ErrorDispatcher : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        val error = logEntry.getTransferObject() as HttpError
        val message = error.message
        logEntry.setError(message)
        ErrorAlert(logEntry).open()
    }

}
