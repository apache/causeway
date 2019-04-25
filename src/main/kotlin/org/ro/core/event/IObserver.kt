package org.ro.core.event

/**
 * @see: https://en.wikipedia.org/wiki/Observer_pattern
 *
 * In the original pattern the relation Observable:Observer 1:n,
 * Here it is n:1 (LogEvent:Observer).
 * 
 * A more descriptive name would be: collector
 *
 * Observer:
 * @item is notified about changes to related LogEntries,
 * @item invokes subsequent links, and
 * @item creates views for object(s).
 */

interface IObserver {
    fun update(le: LogEntry)
}

