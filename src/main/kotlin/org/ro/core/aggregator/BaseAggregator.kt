package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.event.RoXmlHttpRequest
import org.ro.core.model.BaseDisplayable
import org.ro.to.Link
import org.ro.to.TObject

/**
 * An Aggregator:
 * @item is initially created in ResponseHandlers, DisplayObjects, Menus
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
abstract class BaseAggregator {

    open lateinit var dsp: BaseDisplayable

    open fun update(logEntry: LogEntry) {}

    open fun reset(): BaseAggregator {
        //do nothing and
        return this
    }

    open fun getObject(): TObject? {
        return null
    }

    protected fun log(logEntry: LogEntry) {
        logEntry.setUndefined("unexpected")
        console.log("[BaseAggregator.log] unexpected:\n $logEntry}")
        console.log(this::class.simpleName)
        console.log(logEntry)
    }

    fun invoke(link: Link) {
        RoXmlHttpRequest().invoke(link, this)
    }

    fun TObject.getLayoutLink(): Link? {
        var href: String?
        for (l in links) {
            href = l.href
            if (href.isNotEmpty() && href.contains("layout")) {
                return l
            }
        }
        return null
    }

}
