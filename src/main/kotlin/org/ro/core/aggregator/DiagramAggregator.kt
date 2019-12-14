package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.ui.FileAlert
import org.ro.ui.Point

class DiagramAggregator(val at: Point = Point(100,100)) : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        FileAlert(logEntry).open()
    }

}
