package org.ro.core.observer

import org.ro.core.event.IObserver
import org.ro.core.event.LogEntry
import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.Link
import org.ro.to.TObject

abstract class BaseObserver : IObserver {

    @ExperimentalUnsignedTypes
    protected fun log(logEntry: LogEntry) {
        console.log("[ListObserver.update] unexpected:\n $logEntry}")
    }

    fun invoke(link: Link) {
        RoXmlHttpRequest().invoke(link, this)
    }

    fun TObject.getLayoutLink(): Link? {
        var href: String?
        for (l in links) {
            href = l.href
            if (href.isNotEmpty()) {
                //can be "object-layout" >= 1.16
                if (href.endsWith("layout")) {
                    return l
                }
            }
        }
        return null
    }

}
