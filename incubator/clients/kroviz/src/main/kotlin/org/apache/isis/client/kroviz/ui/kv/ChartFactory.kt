package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.event.LogEntry

class ChartFactory {

    fun build(logEventList: MutableList<LogEntry> = mutableListOf<LogEntry>()): EventChart {
        return EventChart(ChartModel())
    }
}
