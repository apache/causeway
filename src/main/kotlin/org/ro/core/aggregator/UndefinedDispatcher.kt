package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.view.UndefinedAlert

class UndefinedDispatcher : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        UndefinedAlert(logEntry).open()
    }

}
