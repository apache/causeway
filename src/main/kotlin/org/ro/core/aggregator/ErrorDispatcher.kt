package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.ui.ErrorAlert

class ErrorDispatcher : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
       ErrorAlert(logEntry).open()
    }

}
