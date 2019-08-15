package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.to.HttpError
import org.ro.view.ErrorAlert

class ErrorAggregator : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        val e = logEntry.getObj() as HttpError
        ErrorAlert(e).open()
    }
}
