package org.ro.org.ro.core.aggregator

import org.ro.core.aggregator.BaseAggregator
import org.ro.core.event.LogEntry
import org.ro.org.ro.ui.FileAlert

class DownloadAggregator(val actionTitle: String) : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        FileAlert(logEntry).open()
    }

}
