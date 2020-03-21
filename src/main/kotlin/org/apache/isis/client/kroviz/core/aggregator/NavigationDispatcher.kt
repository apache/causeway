package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.ui.kv.UiManager

class NavigationDispatcher() : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val obj = logEntry.getTransferObject()
        val result = obj as Menubars
        UiManager.amendMenu(result)
    }

}
