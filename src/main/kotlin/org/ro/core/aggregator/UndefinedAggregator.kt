package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.model.BaseDisplayable
import org.ro.view.UndefinedAlert

class UndefinedAggregator : BaseAggregator() {

    lateinit override var dsp: BaseDisplayable

    override fun update(logEntry: LogEntry) {
        UndefinedAlert(logEntry).open()
    }
}
