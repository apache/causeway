package org.ro.core.observer

import org.ro.core.event.IObserver
import org.ro.core.event.LogEntry
import org.ro.to.Link
import org.ro.to.TObject

abstract class BaseObserver : IObserver {
    protected fun log(le: LogEntry) {
        console.log("[ListObserver.update] unexpected:\n ${le.toString()}")
    }
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