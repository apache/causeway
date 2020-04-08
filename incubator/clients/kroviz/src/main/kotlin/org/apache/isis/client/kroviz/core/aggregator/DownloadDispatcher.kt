package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.ui.FileAlert

class DownloadDispatcher(val actionTitle: String) : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        FileAlert(logEntry).open()
    }

}
