package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.ui.FileAlert

class DownloadDispatcher(val actionTitle: String) : BaseAggregator() {

    override fun update(logEntry: LogEntry, mimeType: String) {
        FileAlert(logEntry).open()
    }

}
