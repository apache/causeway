package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.model.BaseDisplayable
import org.ro.to.mb.Menubars
import org.ro.ui.kv.UiManager

class NavigationDispatcher() : BaseAggregator() {

    override lateinit var dsp: BaseDisplayable

    override fun update(logEntry: LogEntry) {
        val obj = logEntry.getTransferObject()
        val result = obj as Menubars
        UiManager.amendMenu(result)
    }

}
