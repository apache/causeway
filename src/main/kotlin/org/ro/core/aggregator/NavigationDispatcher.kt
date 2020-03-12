package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.to.mb.Menubars
import org.ro.ui.kv.UiManager

class NavigationDispatcher() : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val obj = logEntry.getTransferObject()
        val result = obj as Menubars
        UiManager.amendMenu(result)
    }

}
