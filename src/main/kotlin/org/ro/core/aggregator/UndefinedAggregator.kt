package org.ro.org.ro.core.aggregator

import org.ro.core.aggregator.BaseAggregator
import org.ro.core.event.LogEntry
import org.ro.org.ro.view.UndefinedAlert

class UndefinedAggregator: BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        UndefinedAlert(logEntry).open()
    }
}
