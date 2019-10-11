package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.Link
import org.ro.to.TObject

abstract class BaseAggregator : IAggregator {

    override fun reset() {
        // default is doing nothing
    }

    protected fun log(logEntry: LogEntry) {
        console.log("[BaseAggregator.log] unexpected:\n $logEntry}")
    }

    fun invoke(link: Link) {
        RoXmlHttpRequest().invoke(link, this)
    }

    fun TObject.getLayoutLink(): Link? {
        var href: String?
        for (l in links) {
            href = l.href
            if (href.isNotEmpty() && href.endsWith("layout")) {
                return l
            }
        }
        return null
    }

}
