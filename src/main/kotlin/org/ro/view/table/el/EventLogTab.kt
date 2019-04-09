package org.ro.view.table.el

import org.ro.core.event.LogEntry
import org.ro.view.table.ColDef

class EventLogTab {
    val csList = mutableListOf<ColDef>()

    init {
        csList.add(ColDef(LogEntry::state, 2, " ", null))
        csList.add(ColDef(LogEntry::method, 5))
        csList.add(ColDef(LogEntry::createdAt, 8, "Created", null))
        csList.add(ColDef(LogEntry::updatedAt, 8, "Updated", null))
        csList.add(ColDef(LogEntry::requestLength, 3, "req.len", "request"))
        csList.add(ColDef(LogEntry::offset, 4))
        csList.add(ColDef(LogEntry::duration, 4))
        csList.add(ColDef(LogEntry::responseLength, 5, "resp.len", "response"))
        csList.add(ColDef(LogEntry::cacheHits, 2))
        csList.add(ColDef(LogEntry::menu, 2))
    }
}
