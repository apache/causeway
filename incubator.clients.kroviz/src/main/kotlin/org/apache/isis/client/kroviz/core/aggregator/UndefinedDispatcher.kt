package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.ro.view.UndefinedAlert

class UndefinedDispatcher : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        UndefinedAlert(logEntry).open()
    }

}
