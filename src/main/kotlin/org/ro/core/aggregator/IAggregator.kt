package org.ro.core.aggregator

import org.ro.core.event.LogEntry

/**
 * An IAggregator:
 * @item is initially created in a ResponseHandler, (//TODO except it is called from a menu ???)
 * @item is assigned to at least one LogEntry,
 * @item is passed on to related LogEntries (eg. sibblings in a list, Layout),
 * @item is notified about changes to related LogEntries,
 * @item invokes subsequent links, and
 * @item triggers creation a view for an object or a list.
 *
 * @see: https://www.enterpriseintegrationpatterns.com/patterns/messaging/IAggregator.html
 *
 * Could be named collector or assembler as well.
 */
interface IAggregator {

    fun update(logEntry: LogEntry)

    fun reset() : IAggregator

}
