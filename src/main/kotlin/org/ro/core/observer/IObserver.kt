package org.ro.core.event

/**
 * An Observer:
 * @item is initially created in a ResponseHandler,
 * @item is assigned to a LogEntry,
 * @item is passed on to related LogEntries (eg. sibblings in a list, Layout),
 * @item is notified about changes to related LogEntries,
 * @item invokes subsequent links, and
 * @item creates a view for an object or a list.
 *
 * @see: https://en.wikipedia.org/wiki/Observer_pattern
 *
 * In the original pattern the relation Observable:Observer 1:n,
 * Here it is n:1 (LogEvent:Observer).
 *
 * Could be named collector or assembler as well.
 */
interface IObserver {
    fun update(logEntry: LogEntry)
}

