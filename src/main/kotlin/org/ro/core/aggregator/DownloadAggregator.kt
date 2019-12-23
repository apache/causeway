package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.model.BaseDisplayable
import org.ro.ui.FileAlert

class DownloadAggregator(val actionTitle: String) : BaseAggregator() {

    lateinit override var dsp: BaseDisplayable

    override fun update(logEntry: LogEntry) {
        FileAlert(logEntry).open()
    }

}
